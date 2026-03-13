package com.medlab.db;

import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * Runs the schema.sql DDL on startup (CREATE TABLE IF NOT EXISTS).
 * Safe to call multiple times — idempotent.
 *
 * Future (Spring Boot v2): Replaced by Flyway / Liquibase migrations.
 */
public class SchemaInitializer {

    public static void initialize() {
        System.out.println("[DB] Initializing schema...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // ── users ──────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id            INT          AUTO_INCREMENT PRIMARY KEY,
                    username      VARCHAR(50)  NOT NULL UNIQUE,
                    email         VARCHAR(100) NOT NULL UNIQUE,
                    mobile_number VARCHAR(15)  NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    role          ENUM('ADMIN','RECEPTIONIST','LAB_TECHNICIAN') NOT NULL DEFAULT 'RECEPTIONIST',
                    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
                    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    created_by    VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // ── patients ───────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    id            INT          AUTO_INCREMENT PRIMARY KEY,
                    name          VARCHAR(100) NOT NULL,
                    age           INT          NOT NULL,
                    gender        ENUM('M','F','O') NOT NULL,
                    email         VARCHAR(100) UNIQUE,
                    mobile_number VARCHAR(15)  NOT NULL UNIQUE,
                    address       VARCHAR(255),
                    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    created_by    VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // ── test_orders ────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS test_orders (
                    id          INT          AUTO_INCREMENT PRIMARY KEY,
                    patient_id  INT          NOT NULL,
                    test_name   VARCHAR(100) NOT NULL,
                    ordered_by  VARCHAR(100) NOT NULL,
                    status      ENUM('ORDERED','SAMPLE_COLLECTED','IN_PROGRESS','REPORT_READY','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ORDERED',
                    priority    ENUM('ROUTINE','URGENT','STAT') NOT NULL DEFAULT 'ROUTINE',
                    notes       TEXT,
                    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    created_by  VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
                    FOREIGN KEY (patient_id) REFERENCES patients(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // ── samples ────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS samples (
                    id               INT          AUTO_INCREMENT PRIMARY KEY,
                    order_id         INT          NOT NULL,
                    sample_type      ENUM('BLOOD','URINE','STOOL','SWAB','SPUTUM','CSF','OTHER') NOT NULL,
                    collected_by     VARCHAR(100) NOT NULL,
                    collected_date   DATE         NOT NULL,
                    status           ENUM('COLLECTED','PROCESSING','ANALYSED','REJECTED') NOT NULL DEFAULT 'COLLECTED',
                    rejection_reason VARCHAR(255),
                    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    created_by       VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
                    FOREIGN KEY (order_id) REFERENCES test_orders(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // ── reports ────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reports (
                    id           INT          AUTO_INCREMENT PRIMARY KEY,
                    order_id     INT          NOT NULL,
                    result       TEXT         NOT NULL,
                    remarks      TEXT,
                    normal_range VARCHAR(255),
                    is_abnormal  BOOLEAN      NOT NULL DEFAULT FALSE,
                    prepared_by  VARCHAR(100) NOT NULL,
                    verified_by  VARCHAR(100),
                    report_date  DATE         NOT NULL,
                    status       ENUM('DRAFT','VERIFIED','RELEASED') NOT NULL DEFAULT 'DRAFT',
                    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    created_by   VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
                    FOREIGN KEY (order_id) REFERENCES test_orders(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // ── notifications ──────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id         INT      AUTO_INCREMENT PRIMARY KEY,
                    patient_id INT      NOT NULL,
                    message    TEXT     NOT NULL,
                    type       ENUM('ORDER_CREATED','SAMPLE_COLLECTED','REPORT_READY','GENERAL','ORDER_CANCELLED') NOT NULL,
                    is_read    BOOLEAN  NOT NULL DEFAULT FALSE,
                    is_deleted BOOLEAN  NOT NULL DEFAULT FALSE,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
                    FOREIGN KEY (patient_id) REFERENCES patients(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // ── Default admin user ─────────────────────────────────
            // password: Admin@123  (SHA-256)
            stmt.execute("""
                INSERT IGNORE INTO users
                    (username, email, mobile_number, password_hash, role, created_by)
                VALUES
                    ('admin','admin@medlab.com','9999999999',
                     'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7',
                     'ADMIN','SYSTEM')
            """);

            // ── Seed users (password: Admin@123) ──────────────────
            stmt.execute("""
                INSERT IGNORE INTO users
                    (username, email, mobile_number, password_hash, role, created_by)
                VALUES
                    ('receptionist1','receptionist@medlab.com','8888888888',
                     'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7',
                     'RECEPTIONIST','SYSTEM')
            """);

            stmt.execute("""
                INSERT IGNORE INTO users
                    (username, email, mobile_number, password_hash, role, created_by)
                VALUES
                    ('labtech1','labtech@medlab.com','7777777777',
                     'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7',
                     'LAB_TECHNICIAN','SYSTEM')
            """);

            // ── Seed patients ──────────────────────────────────────
            stmt.execute("""
                INSERT IGNORE INTO patients
                    (name, age, gender, email, mobile_number, created_by)
                VALUES
                    ('John Doe', 22, 'M', 'johndoe@example.com', '1234567890', 'admin')
            """);

            stmt.execute("""
                INSERT IGNORE INTO patients
                    (name, age, gender, email, mobile_number, created_by)
                VALUES
                    ('Jane Doe', 22, 'F', 'janedoe@example.com', '2345678901', 'admin')
            """);

            stmt.execute("""
                INSERT IGNORE INTO patients
                    (name, age, gender, email, mobile_number, created_by)
                VALUES
                    ('Richard Roe', 22, 'M', 'richardroe@example.com', '3456789012', 'admin')
            """);

            System.out.println("[DB] Schema ready.\n");

        } catch (SQLException e) {
            System.err.println("[DB] Schema initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
