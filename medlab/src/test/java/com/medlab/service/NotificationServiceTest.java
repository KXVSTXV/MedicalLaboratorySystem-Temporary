package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.Notification;
import com.medlab.model.User;
import com.medlab.repository.NotificationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepo;
    @InjectMocks private NotificationService service;

    @BeforeEach
    void setUpAuth() {
        User admin = new User(1, "admin", "admin@medlab.com", "9999999999",
                              "hash", "ADMIN", true, false,
                              "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(admin);
    }

    @AfterEach
    void tearDown() {
        AuthContext.logout();
    }

    @Test
    @DisplayName("send: persists notification and does not throw on success")
    void send_persistsRecord() throws SQLException {
        when(notificationRepo.save(any())).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1);
            return n;
        });

        assertDoesNotThrow(() ->
            service.send(1, "Order #5 placed", Notification.Type.ORDER_CREATED));

        verify(notificationRepo).save(any(Notification.class));
    }

    @Test
    @DisplayName("send: does not throw even if DB fails (best-effort)")
    void send_dbFailure_noException() throws SQLException {
        when(notificationRepo.save(any())).thenThrow(new SQLException("DB error"));

        // Should NOT propagate the exception — notification is best-effort
        assertDoesNotThrow(() ->
            service.send(1, "Test message", Notification.Type.GENERAL));
    }

    @Test
    @DisplayName("getAllNotifications: returns list from repo")
    void getAllNotifications_returnsList() throws SQLException {
        List<Notification> expected = List.of(
            new Notification(1, 1, "Order created", "ORDER_CREATED",
                             false, false, "2024-01-01", "2024-01-01", "admin"),
            new Notification(2, 1, "Sample collected", "SAMPLE_COLLECTED",
                             true, false, "2024-01-02", "2024-01-02", "admin")
        );
        when(notificationRepo.findAll()).thenReturn(expected);

        List<Notification> result = service.getAllNotifications();
        assertEquals(2, result.size());
        assertEquals("Order created", result.get(0).getMessage());
    }

    @Test
    @DisplayName("getNotificationsForPatient: filters by patientId")
    void getNotificationsForPatient() throws SQLException {
        List<Notification> expected = List.of(
            new Notification(3, 5, "Report ready", "REPORT_READY",
                             false, false, "2024-01-03", "2024-01-03", "admin")
        );
        when(notificationRepo.findByPatientId(5)).thenReturn(expected);

        List<Notification> result = service.getNotificationsForPatient(5);
        assertEquals(1, result.size());
        assertEquals(Notification.Type.REPORT_READY, result.get(0).getType());
    }

    @Test
    @DisplayName("getUnreadForPatient: returns only unread notifications")
    void getUnreadForPatient() throws SQLException {
        List<Notification> unread = List.of(
            new Notification(4, 2, "Urgent test ordered", "ORDER_CREATED",
                             false, false, "2024-01-04", "2024-01-04", "admin")
        );
        when(notificationRepo.findUnreadByPatient(2)).thenReturn(unread);

        List<Notification> result = service.getUnreadForPatient(2);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    @DisplayName("markAsRead: calls repo markRead")
    void markAsRead() throws SQLException {
        doNothing().when(notificationRepo).markRead(10);

        assertDoesNotThrow(() -> service.markAsRead(10));
        verify(notificationRepo).markRead(10);
    }
}
