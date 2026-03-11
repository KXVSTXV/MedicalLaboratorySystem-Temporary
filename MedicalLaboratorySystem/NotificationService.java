package com.medlab.service;

import com.medlab.model.Notification;
import com.medlab.repository.NotificationRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Notification Service.
 *
 * Currently sends notifications to the console (stdout).
 * Future: becomes a Spring @Service that sends Email/SMS via a
 *         message broker (Kafka/RabbitMQ) in the microservices version.
 */
public class NotificationService {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NotificationRepository notificationRepo;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    /**
     * Sends a notification to the patient — currently prints to console
     * and persists a record so we have an audit trail.
     */
    public void send(int patientId, String message, Notification.Type type) {
        String sentAt = LocalDateTime.now().format(FMT);
        Notification n = new Notification(patientId, message, type, sentAt);

        try {
            notificationRepo.save(n);
        } catch (SQLException e) {
            System.err.println("[Notification] Could not persist notification: " + e.getMessage());
        }

        // ── Console "delivery" ────────────────────────────────────
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.printf ("║  📢 NOTIFICATION [%s]%n", type);
        System.out.printf ("║  To Patient ID : %d%n", patientId);
        System.out.printf ("║  Message       : %s%n", message);
        System.out.printf ("║  Sent At       : %s%n", sentAt);
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    public List<Notification> getNotificationsForPatient(int patientId) throws SQLException {
        return notificationRepo.findByPatientId(patientId);
    }

    public List<Notification> getAllNotifications() throws SQLException {
        return notificationRepo.findAll();
    }
}
