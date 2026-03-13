package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Report;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC DAO for Report.
 * All queries filter is_deleted = FALSE.
 */
public class ReportRepository {

    public Report save(Report report) throws SQLException {
        String sql = """
            INSERT INTO reports
                (order_id, result, remarks, normal_range, is_abnormal,
                 prepared_by, report_date, status, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt    (1, report.getOrderId());
            ps.setString (2, report.getResult());
            ps.setString (3, report.getRemarks());
            ps.setString (4, report.getNormalRange());
            ps.setBoolean(5, report.isAbnormal());
            ps.setString (6, report.getPreparedBy());
            ps.setString (7, report.getReportDate());
            ps.setString (8, report.getStatus().name());
            ps.setString (9, report.getCreatedBy());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) report.setId(keys.getInt(1));
        }
        return report;
    }

    public Optional<Report> findById(int id) throws SQLException {
        String sql = "SELECT * FROM reports WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<Report> findAll() throws SQLException {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE is_deleted = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Report> findByOrderId(int orderId) throws SQLException {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE order_id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void updateStatus(int id, Report.Status status, String verifiedBy) throws SQLException {
        String sql = "UPDATE reports SET status = ?, verified_by = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, verifiedBy);
            ps.setInt   (3, id);
            ps.executeUpdate();
        }
    }

    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE reports SET is_deleted = TRUE, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Private helpers ───────────────────────────────────────────

    private Report mapRow(ResultSet rs) throws SQLException {
        return new Report(
            rs.getInt("id"),
            rs.getInt("order_id"),
            rs.getString("result"),
            rs.getString("remarks"),
            rs.getString("normal_range"),
            rs.getBoolean("is_abnormal"),
            rs.getString("prepared_by"),
            rs.getString("verified_by"),
            rs.getString("report_date"),
            rs.getString("status"),
            rs.getBoolean("is_deleted"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            rs.getString("created_by")
        );
    }
}
