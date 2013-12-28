package petrglad.millinames.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petrglad.millinames.client.GreetingService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.*;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

    private final static Logger LOG = LoggerFactory.getLogger(GreetingServiceImpl.class);
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) throws ServletException {
        initDb();
        super.init(config);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private void initDb() throws ServletException {
        try {
            dataSource = new DriverDataSource(null, "jdbc:hsqldb:file:millinames-database;shutdown=true", "sa", "");
            Flyway flyway = new Flyway();
            flyway.setSqlMigrationPrefix("V");
            flyway.setLocations("db/migration");
            flyway.setInitOnMigrate(true);
            flyway.setDataSource(dataSource);
            int successCount = flyway.migrate();
            LOG.info("Applied migrations {}", successCount);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public String greetServer() throws IllegalArgumentException {

        // See http://hsqldb.org/doc/guide/deployment-chapt.html
        // "Bulk Inserts, Updates and Deletes"

        StringBuilder result = new StringBuilder();
        try {
            Connection conn = dataSource.getConnection();
            try {
                {
                    PreparedStatement stat = conn.prepareStatement("insert into names(first_name, last_name) values (?, ?)");
                    stat.setString(1, "Hooker");
                    stat.setString(2, "DeWitt");
                    stat.execute();
                }
                {
                    Statement stat = conn.createStatement();
                    stat.execute("select first_name, last_name from names");
                    ResultSet rs = stat.getResultSet();
                    while (rs.next()) {
                        result.append(rs.getString("first_name")).append(" ").append(rs.getString("last_name")).append("\n");
                    }
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            LOG.error("DB access error", e);
        }

//        String serverInfo = getServletContext().getServerInfo();
        return "I run " + result.toString();
    }
}
