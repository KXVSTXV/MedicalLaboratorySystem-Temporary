package com.medlab.repository;

import com.medlab.db.DatabaseConnection;
import com.medlab.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Patient.
 *
 * All SQL is written explicitly here so the JDBC pattern is clear.
 * Future: this becomes a JpaRepository<Patient, Integer> interface in Spring Data.
 */
public class PatientRepository {

    public Patient save(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, age, gender, contact) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getName());
            ps.setInt   (2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getContact());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                patient.setId(keys.getInt(1));
            }
        }
        return patient;
    }

    public Optional<Patient> findById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Patient> findAll() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Private helpers ───────────────────────────────────────────

    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("contact")
        );
    }
}
