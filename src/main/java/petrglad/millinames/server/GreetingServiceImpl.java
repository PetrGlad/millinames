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
import java.util.*;
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
        disposeCursors();

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

    private void disposeCursors() {
        synchronized (cursors) {
            for (CursorHolder ch : cursors.values())
                ch.dispose();
            cursors.clear();
        }
    }

    // XXX This won't scale, sloppy resources management
    class CursorHolder {

        public final Connection conn;
        private final ResultSet rs;

        public CursorHolder(boolean orderByFirst) throws SQLException {
            conn = dataSource.getConnection();

            final Statement stat = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            final String orderColumn = orderByFirst ? "first_name" : "last_name";
            stat.execute("select first_name, last_name from names order by " + orderColumn);
            rs = stat.getResultSet();
            rs.setFetchDirection(ResultSet.FETCH_FORWARD);
        }

        public synchronized List<String[]> query(int row, int count) {
            List<String[]> result = new ArrayList<String[]>();
            try {
                int resultCount = 0;
                if (rs.absolute(row + 1)) {
                    do {
                        result.add(new String[]{rs.getString("first_name"), rs.getString("last_name")});
                        resultCount++;
                    } while (rs.next() && resultCount < count);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        public void dispose() {
            try {
                rs.close();
            } catch (SQLException e) {
                LOG.error("Can not close result set", e);
            }
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.error("Can not close connection", e);
            }
        }
    }

    private final Map<Boolean, CursorHolder> cursors = new HashMap<Boolean, CursorHolder>();

    private void initDb() throws ServletException {
        LOG.info("Opening database.");
        try {
            dataSource = new DriverDataSource(null, "jdbc:hsqldb:file:db/millinames-database", "sa", ""); // ;shutdown=true
            doDbMigration();
            LOG.info("Database ready,");
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
        if (!cursors.containsKey(orderByFirst)) {
            synchronized (cursors) {
                if (!cursors.containsKey(orderByFirst)) {
                    try {
                        cursors.put(orderByFirst, new CursorHolder(orderByFirst));
                    } catch (SQLException e) {
                        LOG.error("Can not create cursor", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cursors.get(orderByFirst).query(row, count);
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
        LOG.info("Reinitializing data.");
        disposeCursors();
        conn.setAutoCommit(false);
        final StopWatch sw = new StopWatch();
        sw.start();
        Statement statement = conn.createStatement();
        LOG.info("Deleting old data.");
//        statement.execute("set files log false");
//        statement.execute("checkpoint");
//        statement.execute("drop index names_first");
//        statement.execute("drop index names_last");
//        statement.execute("delete from names");
        statement.execute("drop table names"); // XXX This will interfere with opened cursors
        // XXX duplicates flyway configs:
        statement.execute("create cached table names (\n" +
                "  first_name varchar(10),\n" +
                "  last_name varchar(10)\n" +
                ")");
        statement.execute("create index names_first on names(first_name)");
        statement.execute("create index names_last on names(last_name)");
        LOG.info("Data deleted.");
        // See http://hsqldb.org/doc/guide/deployment-chapt.html
        // "Bulk Inserts, Updates and Deletes"
        // statement.execute("set files log false");
        // statement.execute("checkpoint");
        final PreparedStatement stat = conn.prepareStatement("insert into names(first_name, last_name) values (?, ?)");
        final Random r = new Random();
        int N = 1000000;
        char[] buffer = new char[10];
        LOG.info("Inserting new data.");
        for (int i = 0; i < N; i++) {
            stat.setString(1, genName(r, buffer));
            stat.setString(2, genName(r, buffer));
            stat.execute();
            if (i % 5000 == 0)       {
                conn.commit();
                LOG.info("{} rows inserted.", i);
            }
        }
        conn.commit();
        LOG.info("New data inserted.");

//        statement.execute("set files log true");
//        statement.execute("checkpoint");
//        LOG.info("Indexing");

        conn.commit();
        disposeCursors(); // XXX better not allow querying while regenerating data
        sw.stop();
        LOG.info("Regenerated data in " + (sw.getTotalTimeMillis() / 1000) + " sec.");
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
