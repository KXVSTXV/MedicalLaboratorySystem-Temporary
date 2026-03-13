package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.Notification;
import com.medlab.repository.NotificationRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Notification Service.
 *
 * Currently prints to console (CLI "delivery") and persists an audit record.
 *
 * Future (Spring Boot v2): becomes a Spring @Service that publishes to
 * Kafka/RabbitMQ; downstream consumers send Email/SMS.
 */
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    /**
     * Sends a notification: persists record + prints to console.
     */
    public void send(int patientId, String message, Notification.Type type) {
        Notification n = new Notification(patientId, message, type,
                                          AuthContext.getCurrentUsername());
        try {
            notificationRepo.save(n);
        } catch (SQLException e) {
            System.err.println("[Notification] Could not persist notification: " + e.getMessage());
        }

        // ── Console "delivery" ────────────────────────────────────
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.printf ("║  📢 NOTIFICATION  [%-25s]║%n", type);
        System.out.printf ("║  Patient ID : %-33d║%n", patientId);
        System.out.printf ("║  Message    : %-33s║%n",
            message.length() > 33 ? message.substring(0, 30) + "..." : message);
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    public List<Notification> getNotificationsForPatient(int patientId) throws SQLException {
        return notificationRepo.findByPatientId(patientId);
    }

    public List<Notification> getUnreadForPatient(int patientId) throws SQLException {
        return notificationRepo.findUnreadByPatient(patientId);
    }

    public List<Notification> getAllNotifications() throws SQLException {
        return notificationRepo.findAll();
    }

    public void markAsRead(int notificationId) throws SQLException {
        notificationRepo.markRead(notificationId);
    }
}
