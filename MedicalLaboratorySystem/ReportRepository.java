package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Report;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Data Access Object for Report.
 *
 * Future: becomes JpaRepository<Report, Integer> in Spring Data JPA.
 */
public class ReportRepository {

    public Report save(Report report) throws SQLException {
        String sql = """
            INSERT INTO reports (order_id, result, remarks, prepared_by, report_date, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, report.getOrderId());
            ps.setString(2, report.getResult());
            ps.setString(3, report.getRemarks());
            ps.setString(4, report.getPreparedBy());
            ps.setString(5, report.getReportDate());
            ps.setString(6, report.getStatus().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) report.setId(keys.getInt(1));
        }
        return report;
    }

    public Optional<Report> findById(int id) throws SQLException {
        String sql = "SELECT * FROM reports WHERE id = ?";
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
        String sql = "SELECT * FROM reports";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Report> findByOrderId(int orderId) throws SQLException {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void updateStatus(int id, Report.Status status) throws SQLException {
        String sql = "UPDATE reports SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, id);
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
            rs.getString("prepared_by"),
            rs.getString("report_date"),
            rs.getString("status")
        );
    }
}
