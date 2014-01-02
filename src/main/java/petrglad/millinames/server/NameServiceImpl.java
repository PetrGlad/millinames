package petrglad.millinames.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petrglad.millinames.client.NameService;
import petrglad.millinames.server.requestfactory.NameLocator;

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
public class NameServiceImpl extends RemoteServiceServlet implements NameService {

    private static class DataHolder {
        public final String[][] data; // [row, column]
        public final Integer[][] indexes; // [index, position]

        private DataHolder(String[][] data, Integer[][] indexes) {
            this.data = data;
            this.indexes = indexes;
        }

        public String[] get(int row, int index) {
            return data[indexes[index][row]];
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(NameServiceImpl.class);
    private static final String ABC_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final CharSequence CAPS_CHARS = ABC_CHARS.toUpperCase();
    private static final CharSequence NAME_CHARS = ABC_CHARS + "0123456789";

    private static final AtomicBoolean DATA_LOCKED = new AtomicBoolean(false);
    private static final int COLUMN_COUNT = 2;

    private DataSource dataSource;

    private DataHolder data;

    @Override
    public void init(ServletConfig config) throws ServletException {
        LOG.info("Initializing servlet");
        super.init(config);
        initDb();
        NameLocator.setService(this); // TODO Use DI instead.

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
        disposeCached();
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
        LOG.info("Opening database.");
        try {
            dataSource = new DriverDataSource(null, "jdbc:hsqldb:file:db/millinames-database", "sa", ""); // ;shutdown=true
            doDbMigration();
            LOG.info("Database is ready,");
            cacheData();
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
    public List<String[]> getBatch(final int row, final int count, final int orderColumn) {
        cacheData();
        assert data != null;
        List<String[]> result = new ArrayList<String[]>(count);
        for (int i = row; i < row + count; i++)
            result.add(data.get(i, orderColumn));
        return result;
    }

    private void cacheData() {
        if (data == null) {
            synchronized (this) {
                if (data == null) {
                    data = sortData();
                }
            }
        }
    }

    private void disposeCached() {
        synchronized (this) {
            data = null;
        }
    }

    private DataHolder sortData() {
        LOG.info("Loading data.");
        final String[][] records = loadAll();
        final Integer[][] indexes = new Integer[COLUMN_COUNT][];
        LOG.info("Indexing.");
        for (int c = 0; c < indexes.length; c++)
            indexes[c] = sortIndexed(records, c);
        LOG.info("Cache is initialized.");
        return new DataHolder(records, indexes);
    }

    private String[][] loadAll() {
        return withConnection(new Handler<Connection, String[][]>() {
            @Override
            public String[][] put(Connection conn) throws SQLException {
                Statement stat = conn.createStatement();
                stat.execute("select count(*) from names");
                int count;
                {
                    ResultSet rs = stat.getResultSet();
                    rs.next();
                    count = rs.getInt(1);
                }
                stat.execute("select first_name, last_name from names");
                ResultSet rs = stat.getResultSet();
                String[][] result = new String[count][];
                int i = 0;
                while (rs.next()) {
                    result[i++] = new String[]{rs.getString("first_name"), rs.getString("last_name")};
                }
                return result;
            }
        });
    }

    private Integer[] sortIndexed(final String[][] records, final int column) {
        long t = System.currentTimeMillis();
        Integer[] index = new Integer[records.length];
        for (int i = 0; i < records.length; i++) {
            index[i] = i;
        }
        Arrays.sort(index, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return records[o1][column].compareTo(records[o2][column]);
            }
        });
        LOG.info("Indexing {} complete in {}", column, System.currentTimeMillis() - t);
        return index;
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

    private int regenerateData(Connection conn) throws SQLException {
        LOG.info("Reinitializing data.");
        conn.setAutoCommit(false);
        final StopWatch sw = new StopWatch();
        sw.start();
        Statement statement = conn.createStatement();
        LOG.info("Deleting old data.");
        statement.execute("drop table names"); // XXX This will interfere with opened cursors
        // XXX duplicates flyway configs:
        statement.execute("create cached table names (\n" +
                "  first_name varchar(10),\n" +
                "  last_name varchar(10)\n" +
                ")");
        LOG.info("Data deleted.");
        // See also http://hsqldb.org/doc/guide/deployment-chapt.html
        // "Bulk Inserts, Updates and Deletes"
        final PreparedStatement stat = conn.prepareStatement("insert into names(first_name, last_name) values (?, ?)");
        int N = 1000000;
        final Random r = new Random();
        char[] buffer = new char[10];
        LOG.info("Inserting new data.");
        for (int i = 0; i < N; i++) {
            stat.setString(1, genName(r, buffer));
            stat.setString(2, genName(r, buffer));
            stat.addBatch();
            if (i % 5000 == 0) {
                stat.executeBatch();
                conn.commit();
            }
            if (i % 100000 == 0) {
                LOG.info("{} rows inserted.", i);
            }
        }
        stat.executeBatch();
        conn.commit();
        LOG.info("New data inserted.");
        disposeCached();
        cacheData();
        sw.stop();
        LOG.info("Regenerated data in " + (sw.getTotalTimeMillis() / 1000) + " sec.");
        return N;
    }

    static String genName(Random r, char[] buffer) {
        int len = r.nextInt(5) + 5;
        int i = 0;
        buffer[i++] = CAPS_CHARS.charAt(r.nextInt(CAPS_CHARS.length()));
        while (i < len) {
            buffer[i++] = NAME_CHARS.charAt(r.nextInt(NAME_CHARS.length()));
        }
        return new String(buffer, 0, len);
    }
}
