package petrglad.millinames.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petrglad.millinames.client.GreetingService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

    private static final Logger LOG = LoggerFactory.getLogger(GreetingServiceImpl.class);
    private static final String ABC_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final CharSequence CAPS_CHARS = ABC_CHARS.toUpperCase();
    private static final CharSequence NAME_CHARS = ABC_CHARS + "0123456789";

    private static final AtomicBoolean DATA_LOCKED = new AtomicBoolean(false);
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) throws ServletException {
        LOG.info("Initializing servlet");
        super.init(config);
        initDb();
    }


    public interface Handler<T, P> {
        P put(T v) throws SQLException;
    }

    public <T> T withConnection(Handler<Connection, T> handler) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return handler.put(conn);
        } catch (SQLException e) {
            LOG.error("Database error", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOG.error("Can not close connection", e);
                }
        }
    }

    @Override
    public void destroy() {
        LOG.info("Destroying servlet");
//        ((ComboPooledDataSource)dataSource).close();
        withConnection(new Handler<Connection, Void>() {
            @Override
            public Void put(Connection conn) throws SQLException {
                conn.createStatement().execute("shutdown");
                conn.commit();
                return null;
            }
        });
        super.destroy();
    }

    private void initDb() throws ServletException {
        try {
//            ComboPooledDataSource cpds = new ComboPooledDataSource();
//            cpds.setDriverClass( "org.hsqldb.jdbcDriver" );
//            cpds.setJdbcUrl( "jdbc:hsqldb:file:db/millinames-database;shutdown=true" );
//            cpds.setUser("sa");
//            cpds.setPassword("");
//            cpds.setMinPoolSize(1);
//            cpds.setAcquireIncrement(1);
//            cpds.setMaxPoolSize(3);
//            dataSource = cpds;
            dataSource = new DriverDataSource(null, "jdbc:hsqldb:file:db/millinames-database", "sa", ""); // ;shutdown=true
            doDbMigration();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void doDbMigration() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSqlMigrationPrefix("V");
        flyway.setLocations("db/migration");
        flyway.setInitOnMigrate(true);
        int successCount = flyway.migrate();
        LOG.info("Applied migrations {}", successCount);
    }

    @Override
    public List<String[]> getBatch(final int row, final int count, final boolean orderByFirst) {
        return withConnection(new Handler<Connection, List<String[]>>() {
            @Override
            public List<String[]> put(Connection conn) throws SQLException {
                // FIXME This is prototype stub
                final Statement stat = conn.createStatement();
                final String orderColumn = orderByFirst ? "first_name" : "last_name";
                stat.execute("select limit " + row + "," + count + " first_name, last_name from names order by " + orderColumn);
                //stat.execute("select top 20 first_name, last_name from names order by " + orderColumn);
                final ResultSet rs = stat.getResultSet();
                List<String[]> result = new ArrayList<String[]>();
                while (rs.next()) {
                    result.add(new String[]{rs.getString("first_name"), rs.getString("last_name")});
                }
                return result;
            }
        });
    }

    @Override
    public String regenerate() {
        LOG.debug("Regenerate invoked.");
        try {
            if (!DATA_LOCKED.compareAndSet(false, true)) {
                return "Data is locked at the moment. Retry later.";
            }
            return withConnection(new Handler<Connection, String>() {
                @Override
                public String put(Connection conn) throws SQLException {
                    int count = regenerateData(conn);
                    return "Generated " + count + " records.";
                }
            });
        } finally {
            DATA_LOCKED.compareAndSet(true, false);
        }
    }

    public int regenerateData(Connection conn) throws SQLException {
        // See http://hsqldb.org/doc/guide/deployment-chapt.html
        // "Bulk Inserts, Updates and Deletes"
        LOG.info("Reinitializing data.");
        conn.setAutoCommit(false);
        final PreparedStatement stat = conn.prepareStatement("insert into names(first_name, last_name) values (?, ?)");
        final StopWatch sw = new StopWatch();
        sw.start();
        Statement statement = conn.createStatement();
//        statement.execute("set files log false");
//        statement.execute("checkpoint");
//        statement.execute("drop index names_first");
//        statement.execute("drop index names_last");
        LOG.info("Deleting old data.");
        statement.execute("delete from names"); // More radical solution would be to drop table altogether and re-create it.
        LOG.info("Data deleted.");
        final Random r = new Random();
        int N = 1000000;
        char[] buffer = new char[10];
        for (int i = 0; i < N; i++) {
            stat.setString(1, genName(r, buffer));
            stat.setString(2, genName(r, buffer));
            stat.execute();
            if (i % 5000 == 0)
                conn.commit();
        }
//        conn.commit();
//        statement.execute("create index names_first on names(first_name)");
//        statement.execute("create index names_last on names(last_name)");
        conn.commit();
//        statement.execute("set files log true");
//        statement.execute("checkpoint");
        sw.stop();
        LOG.info("Regenerated data in " + sw.getTotalTimeMillis() + "mSec.");
        return N;
    }

    private String genName(Random r, char[] buffer) {
        int len = r.nextInt(5) + 5;
        int i = 0;
        buffer[i++] = CAPS_CHARS.charAt(r.nextInt(CAPS_CHARS.length()));
        while (i < len) {
            buffer[i++] = NAME_CHARS.charAt(r.nextInt(NAME_CHARS.length()));
        }
        return new String(buffer, 0, len);
    }
}
