package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for Notification.
 *
 * Future: becomes JpaRepository<Notification, Integer> in Spring Data JPA.
 */
public class NotificationRepository {

    public Notification save(Notification notification) throws SQLException {
        String sql = """
            INSERT INTO notifications (patient_id, message, type, sent_at)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, notification.getPatientId());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getSentAt());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) notification.setId(keys.getInt(1));
        }
        return notification;
    }

    public List<Notification> findByPatientId(int patientId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE patient_id = ? ORDER BY sent_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Notification> findAll() throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY sent_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── Private helpers ───────────────────────────────────────────

    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
            rs.getInt("id"),
            rs.getInt("patient_id"),
            rs.getString("message"),
            rs.getString("type"),
            rs.getString("sent_at")
        );
    }
}
