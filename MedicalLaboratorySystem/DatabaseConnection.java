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
