package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.Patient;
import com.medlab.model.User;
import com.medlab.repository.PatientRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Patient management.
 *
 * Authorization:
 *   - ADMIN, RECEPTIONIST can register and view patients.
 *   - LAB_TECHNICIAN has read-only access.
 *
 * Future: becomes a Spring @Service with @Transactional.
 */
public class PatientService {

    private final PatientRepository patientRepo;

    public PatientService(PatientRepository patientRepo) {
        this.patientRepo = patientRepo;
    }

    public Patient registerPatient(String name, int age, String gender,
                                   String email, String mobileNumber,
                                   String address) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.RECEPTIONIST);

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Patient name cannot be empty.");
        if (age <= 0 || age > 150)
            throw new IllegalArgumentException("Invalid age: " + age);
        if (mobileNumber == null || mobileNumber.isBlank())
            throw new IllegalArgumentException("Mobile number is required.");
        if (patientRepo.existsByMobile(mobileNumber.trim()))
            throw new IllegalArgumentException("Mobile number already registered: " + mobileNumber);

        Patient patient = new Patient(
            name.trim(), age, gender.trim().toUpperCase(),
            email != null ? email.trim() : null,
            mobileNumber.trim(), address != null ? address.trim() : null,
            AuthContext.getCurrentUsername()
        );
        return patientRepo.save(patient);
    }

    public Optional<Patient> findById(int id) throws SQLException {
        return patientRepo.findById(id);
    }

    public List<Patient> getAllPatients() throws SQLException {
        return patientRepo.findAll();
    }

    public List<Patient> searchByName(String name) throws SQLException {
        return patientRepo.searchByName(name);
    }

    /** Soft-delete (ADMIN only) */
    public void deletePatient(int id) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN);
        if (patientRepo.findById(id).isEmpty())
            throw new IllegalArgumentException("Patient #" + id + " not found.");
        patientRepo.softDelete(id, AuthContext.getCurrentUsername());
        System.out.println("[PatientService] Patient #" + id + " soft-deleted.");
    }
}
