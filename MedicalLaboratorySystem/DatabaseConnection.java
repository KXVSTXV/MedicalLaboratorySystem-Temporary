package com.medlab.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages a single shared JDBC connection to the SQLite database.
 * In a future Spring Boot version, this becomes a DataSource bean.
 */
public class DatabaseConnection {

    // SQLite stores everything in one file next to your jar
    private static final String URL = "jdbc:sqlite:medlab.db";

    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Explicitly load the SQLite driver class.
                // This is required when the jar is added manually (e.g. IntelliJ module dependency)
                // rather than via a build tool like Maven/Gradle.
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "SQLite JDBC driver not found on classpath.\n" +
                                "Fix: File → Project Structure → Modules → Dependencies → '+' → " +
                                "add sqlite-jdbc-x.x.x.jar", e);
            }
            connection = DriverManager.getConnection(URL);
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
