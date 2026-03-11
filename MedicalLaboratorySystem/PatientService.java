package com.medlab.service;

import com.medlab.model.Patient;
import com.medlab.repository.PatientRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Patient registration and lookup.
 *
 * Future: becomes a Spring @Service with @Transactional methods.
 */
public class PatientService {

    private final PatientRepository patientRepo;

    public PatientService(PatientRepository patientRepo) {
        this.patientRepo = patientRepo;
    }

    public Patient registerPatient(String name, int age, String gender, String contact)
            throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Patient name cannot be empty.");
        }
        if (age <= 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age: " + age);
        }
        Patient patient = new Patient(name.trim(), age, gender.trim().toUpperCase(), contact.trim());
        return patientRepo.save(patient);
    }

    public Optional<Patient> findById(int id) throws SQLException {
        return patientRepo.findById(id);
    }

    public List<Patient> getAllPatients() throws SQLException {
        return patientRepo.findAll();
    }
}
