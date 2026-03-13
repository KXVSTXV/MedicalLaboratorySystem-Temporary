package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC DAO for Patient.
 * All queries filter out is_deleted = true.
 *
 * Future: becomes JpaRepository<Patient, Integer>
 */
public class PatientRepository {

    public Patient save(Patient patient) throws SQLException {
        String sql = """
            INSERT INTO patients (name, age, gender, email, mobile_number, address, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getName());
            ps.setInt   (2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getMobileNumber());
            ps.setString(6, patient.getAddress());
            ps.setString(7, patient.getCreatedBy());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) patient.setId(keys.getInt(1));
        }
        return patient;
    }

    public Optional<Patient> findById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<Patient> findAll() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE is_deleted = FALSE ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Patient> searchByName(String name) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE name LIKE ? AND is_deleted = FALSE ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Soft-delete */
    public void softDelete(int id, String deletedBy) throws SQLException {
        String sql = "UPDATE patients SET is_deleted = TRUE, updated_at = NOW(), created_by = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deletedBy);
            ps.setInt   (2, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByMobile(String mobile) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE mobile_number = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mobile);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // ── Private helpers ───────────────────────────────────────────

    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("email"),
            rs.getString("mobile_number"),
            rs.getString("address"),
            rs.getBoolean("is_deleted"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            rs.getString("created_by")
        );
    }
}
