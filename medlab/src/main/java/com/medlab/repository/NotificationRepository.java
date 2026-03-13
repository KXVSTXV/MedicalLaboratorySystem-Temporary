package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC DAO for Notification.
 * All queries filter is_deleted = FALSE.
 */
public class NotificationRepository {

    public Notification save(Notification notification) throws SQLException {
        String sql = """
            INSERT INTO notifications (patient_id, message, type, created_by)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, notification.getPatientId());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getCreatedBy());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) notification.setId(keys.getInt(1));
        }
        return notification;
    }

    public List<Notification> findByPatientId(int patientId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE patient_id = ? AND is_deleted = FALSE ORDER BY created_at DESC";
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
        String sql = "SELECT * FROM notifications WHERE is_deleted = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void markRead(int id) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Notification> findUnreadByPatient(int patientId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE patient_id = ? AND is_read = FALSE AND is_deleted = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
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
            rs.getBoolean("is_read"),
            rs.getBoolean("is_deleted"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            rs.getString("created_by")
        );
    }
}
