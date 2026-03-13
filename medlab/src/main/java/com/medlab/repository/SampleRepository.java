package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC DAO for Sample.
 * All queries filter is_deleted = FALSE.
 */
public class SampleRepository {

    public Sample save(Sample sample) throws SQLException {
        String sql = """
            INSERT INTO samples
                (order_id, sample_type, collected_by, collected_date, status, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, sample.getOrderId());
            ps.setString(2, sample.getSampleType().name());
            ps.setString(3, sample.getCollectedBy());
            ps.setString(4, sample.getCollectedDate());
            ps.setString(5, sample.getStatus().name());
            ps.setString(6, sample.getCreatedBy());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) sample.setId(keys.getInt(1));
        }
        return sample;
    }

    public Optional<Sample> findById(int id) throws SQLException {
        String sql = "SELECT * FROM samples WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<Sample> findAll() throws SQLException {
        List<Sample> list = new ArrayList<>();
        String sql = "SELECT * FROM samples WHERE is_deleted = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Sample> findByOrderId(int orderId) throws SQLException {
        List<Sample> list = new ArrayList<>();
        String sql = "SELECT * FROM samples WHERE order_id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void updateStatus(int id, Sample.Status status, String rejectionReason) throws SQLException {
        String sql = "UPDATE samples SET status = ?, rejection_reason = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, rejectionReason);
            ps.setInt   (3, id);
            ps.executeUpdate();
        }
    }

    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE samples SET is_deleted = TRUE, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Private helpers ───────────────────────────────────────────

    private Sample mapRow(ResultSet rs) throws SQLException {
        return new Sample(
            rs.getInt("id"),
            rs.getInt("order_id"),
            rs.getString("sample_type"),
            rs.getString("collected_by"),
            rs.getString("collected_date"),
            rs.getString("status"),
            rs.getString("rejection_reason"),
            rs.getBoolean("is_deleted"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            rs.getString("created_by")
        );
    }
}
