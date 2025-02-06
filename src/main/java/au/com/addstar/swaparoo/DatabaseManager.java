package au.com.addstar.swaparoo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public abstract class DatabaseManager {
    private SwaparooPlugin plugin;
    private HikariDataSource dataSource;
    private String className;

    public DatabaseManager(SwaparooPlugin plugin) {
        this.plugin = plugin;
        this.className = getClass().getSimpleName();
        configure();
    }

    /**
     * Initializes the database connection pool with parameters from a properties file.
     *
     * @throws SQLException if an error occurs while initializing the pool
     */
    public void initializePool() throws SQLException {
        // Define the path to the <class>.properties file in the data folder
        String propName = className.toLowerCase() + ".properties";
        File propFile = new File(plugin.getDataFolder(), propName);
        SwaparooPlugin.debugMsg(className + ": Loading Hikari properties...");
        if (!propFile.exists()) {
            // Create the treasuresdb.properties file if it doesn't exist
            SwaparooPlugin.debugMsg(className + ": Creating " + propName + " file...");
            plugin.saveResource(propName, false);
        }

        // Load the configuration properties from the file
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(propFile)) {
            properties.load(input);
        } catch (IOException e) {
            plugin.getLogger().severe(className + ": Failed to load " + propName + " file!");
            e.printStackTrace();
            throw new RuntimeException("Could not initialize database configuration", e);
        }

        // Initialize HikariConfig with the loaded properties
        SwaparooPlugin.debugMsg(className + ": Initializing Hikari connection pool...");
        HikariConfig config = new HikariConfig(propFile.getAbsolutePath());
        config.setPoolName(className + "Pool");
        config.setConnectionTestQuery("/* ping */ SELECT 1");
        properties.forEach((key, value) -> config.addDataSourceProperty(key.toString(), value.toString()));

        if (config.getJdbcUrl() == null || config.getJdbcUrl().isEmpty()) {
            plugin.getLogger().severe(className + ": jdbcUrl is missing or invalid!");
            throw new RuntimeException("jdbcUrl is required for HikariCP!");
        }

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Retrieves a connection from the connection pool.
     *
     * @return a {@link Connection} object
     * @throws SQLException if a connection cannot be retrieved
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool is not initialized.");
        }
        return dataSource.getConnection();
    }

    /**
     * Executes an update (INSERT, UPDATE, DELETE) statement on the database.
     *
     * @param sql the SQL statement to execute
     * @param params the parameters for the SQL statement
     * @return the number of rows affected
     * @throws SQLException if an error occurs during execution
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = prepareStatement(connection, sql, params)) {
            return statement.executeUpdate();
        }
    }

    /**
     * Executes a query (SELECT) statement on the database and returns the result.
     *
     * @param conn the database connection
     * @param query the SQL query to execute
     * @param params the parameters for the SQL query
     * @return a {@link ResultSet} containing the query results
     * @throws SQLException if an error occurs during execution
     */
    public ResultSet executeQuery(Connection conn, String query, Object... params) throws SQLException {
        //SwaparooPlugin.debugMsg(className + ": executeQuery: " + query);
        PreparedStatement statement = prepareStatement(conn, query, params);
        return statement.executeQuery();
    }

    /**
     * Prepares a {@link PreparedStatement} with the provided SQL and parameters.
     *
     * @param connection the database connection
     * @param sql        the SQL statement
     * @param params     the parameters for the SQL statement
     * @return a {@link PreparedStatement} object
     * @throws SQLException if an error occurs during preparation
     */
    private PreparedStatement prepareStatement(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    /**
     * Closes the database connection pool and releases resources.
     */
    public void closePool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Abstract method to be implemented by subclasses for specific initialization logic.
     */
    public void configure() {
        // Implement in subclass
    }
}
