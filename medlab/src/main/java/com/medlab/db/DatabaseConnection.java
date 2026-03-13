package com.medlab.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages a single shared JDBC connection to MySQL.
 *
 * Configuration is read from system properties or falls back to defaults.
 * In production, pass -Dmedlab.db.url=... -Dmedlab.db.user=... -Dmedlab.db.password=...
 *
 * Future (Spring Boot v2): Replaced by a DataSource bean in application.yml
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL      = "jdbc:mysql://localhost:3306/medlab_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER     = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url  = System.getProperty("medlab.db.url",      DEFAULT_URL);
            String user = System.getProperty("medlab.db.user",     DEFAULT_USER);
            String pass = System.getProperty("medlab.db.password", DEFAULT_PASSWORD);

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "MySQL JDBC driver not found on classpath.\n" +
                    "Fix: Add mysql-connector-j-x.x.x.jar to the lib/ folder.", e);
            }

            connection = DriverManager.getConnection(url, user, pass);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
