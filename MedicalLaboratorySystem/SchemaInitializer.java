package com.medlab.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs DDL statements to create tables if they don't exist.
 * Called once at application startup.
 *
 * Future: In Spring Boot this becomes schema.sql / Liquibase / Flyway.
 */
public class SchemaInitializer {

    public static void initialize() {
        System.out.println("[DB] Initializing schema...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // ---------- PATIENTS ----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    name        TEXT    NOT NULL,
                    age         INTEGER NOT NULL,
                    gender      TEXT    NOT NULL,
                    contact     TEXT    NOT NULL
                )
            """);

            // ---------- TEST ORDERS ----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS test_orders (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id    INTEGER NOT NULL,
                    test_name     TEXT    NOT NULL,
                    ordered_by    TEXT    NOT NULL,
                    status        TEXT    NOT NULL DEFAULT 'PENDING',
                    ordered_date  TEXT    NOT NULL,
                    FOREIGN KEY(patient_id) REFERENCES patients(id)
                )
            """);

            // ---------- SAMPLES ----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS samples (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id        INTEGER NOT NULL,
                    sample_type     TEXT    NOT NULL,
                    collected_by    TEXT    NOT NULL,
                    collected_date  TEXT    NOT NULL,
                    status          TEXT    NOT NULL DEFAULT 'COLLECTED',
                    FOREIGN KEY(order_id) REFERENCES test_orders(id)
                )
            """);

            // ---------- REPORTS ----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reports (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id        INTEGER NOT NULL,
                    result          TEXT    NOT NULL,
                    remarks         TEXT,
                    prepared_by     TEXT    NOT NULL,
                    report_date     TEXT    NOT NULL,
                    status          TEXT    NOT NULL DEFAULT 'DRAFT',
                    FOREIGN KEY(order_id) REFERENCES test_orders(id)
                )
            """);

            // ---------- NOTIFICATIONS ----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id  INTEGER NOT NULL,
                    message     TEXT    NOT NULL,
                    type        TEXT    NOT NULL,
                    sent_at     TEXT    NOT NULL,
                    FOREIGN KEY(patient_id) REFERENCES patients(id)
                )
            """);

            System.out.println("[DB] Schema ready.\n");

        } catch (SQLException e) {
            System.err.println("[DB] Schema initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
