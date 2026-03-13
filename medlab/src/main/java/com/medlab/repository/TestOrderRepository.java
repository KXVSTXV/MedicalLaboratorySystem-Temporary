package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.TestOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC DAO for TestOrder.
 * All queries filter is_deleted = FALSE.
 *
 * Future: becomes JpaRepository<TestOrder, Integer>
 */
public class TestOrderRepository {

    public TestOrder save(TestOrder order) throws SQLException {
        String sql = """
            INSERT INTO test_orders
                (patient_id, test_name, ordered_by, status, priority, notes, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, order.getPatientId());
            ps.setString(2, order.getTestName());
            ps.setString(3, order.getOrderedBy());
            ps.setString(4, order.getStatus().name());
            ps.setString(5, order.getPriority().name());
            ps.setString(6, order.getNotes());
            ps.setString(7, order.getCreatedBy());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) order.setId(keys.getInt(1));
        }
        return order;
    }

    public Optional<TestOrder> findById(int id) throws SQLException {
        String sql = "SELECT * FROM test_orders WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<TestOrder> findAll() throws SQLException {
        List<TestOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM test_orders WHERE is_deleted = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<TestOrder> findByPatientId(int patientId) throws SQLException {
        List<TestOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM test_orders WHERE patient_id = ? AND is_deleted = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void updateStatus(int id, TestOrder.Status status, String updatedBy) throws SQLException {
        String sql = "UPDATE test_orders SET status = ?, updated_at = NOW() WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, id);
            ps.executeUpdate();
        }
    }

    /** Soft-delete (cancel) */
    public void softDelete(int id, String deletedBy) throws SQLException {
        String sql = """
            UPDATE test_orders
            SET is_deleted = TRUE, status = 'CANCELLED', updated_at = NOW()
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Private helpers ───────────────────────────────────────────

    private TestOrder mapRow(ResultSet rs) throws SQLException {
        return new TestOrder(
            rs.getInt("id"),
            rs.getInt("patient_id"),
            rs.getString("test_name"),
            rs.getString("ordered_by"),
            rs.getString("status"),
            rs.getString("priority"),
            rs.getString("notes"),
            rs.getBoolean("is_deleted"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            rs.getString("created_by")
        );
    }
}
