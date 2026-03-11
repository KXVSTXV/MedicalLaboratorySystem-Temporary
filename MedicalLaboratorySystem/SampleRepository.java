package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Data Access Object for Sample.
 *
 * Future: becomes JpaRepository<Sample, Integer> in Spring Data JPA.
 */
public class SampleRepository {

    public Sample save(Sample sample) throws SQLException {
        String sql = """
            INSERT INTO samples (order_id, sample_type, collected_by, collected_date, status)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, sample.getOrderId());
            ps.setString(2, sample.getSampleType());
            ps.setString(3, sample.getCollectedBy());
            ps.setString(4, sample.getCollectedDate());
            ps.setString(5, sample.getStatus().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) sample.setId(keys.getInt(1));
        }
        return sample;
    }

    public Optional<Sample> findById(int id) throws SQLException {
        String sql = "SELECT * FROM samples WHERE id = ?";
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
        String sql = "SELECT * FROM samples";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Sample> findByOrderId(int orderId) throws SQLException {
        List<Sample> list = new ArrayList<>();
        String sql = "SELECT * FROM samples WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void updateStatus(int id, Sample.Status status) throws SQLException {
        String sql = "UPDATE samples SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, id);
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
            rs.getString("status")
        );
    }
}
