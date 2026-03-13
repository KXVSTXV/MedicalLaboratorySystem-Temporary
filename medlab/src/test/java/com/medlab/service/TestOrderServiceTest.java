package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.*;
import com.medlab.repository.PatientRepository;
import com.medlab.repository.TestOrderRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TestOrderService.
 *
 * Uses Mockito to mock DAO layer — no real DB required.
 */
@ExtendWith(MockitoExtension.class)
class TestOrderServiceTest {

    @Mock private TestOrderRepository   orderRepo;
    @Mock private PatientRepository     patientRepo;
    @Mock private NotificationService   notificationService;

    @InjectMocks private TestOrderService service;

    private static final Patient PATIENT = new Patient(
        1, "John Doe", 30, "M", "john@example.com", "9876543210",
        "123 Main St", false, "2024-01-01 10:00:00", "2024-01-01 10:00:00", "admin"
    );

    @BeforeEach
    void setUpAuth() {
        // Simulate a logged-in receptionist
        User user = new User(1, "receptionist1", "r@medlab.com", "8888888888",
                             "hash", "RECEPTIONIST", true, false,
                             "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(user);
    }

    @AfterEach
    void tearDown() {
        AuthContext.logout();
    }

    // ── placeOrder ────────────────────────────────────────────────

    @Test
    @DisplayName("placeOrder: happy path creates order and sends notification")
    void placeOrder_success() throws Exception {
        when(patientRepo.findById(1)).thenReturn(Optional.of(PATIENT));
        when(orderRepo.save(any())).thenAnswer(inv -> {
            TestOrder o = inv.getArgument(0);
            o.setId(101);
            return o;
        });
        doNothing().when(notificationService).send(anyInt(), anyString(), any());

        TestOrder order = service.placeOrder(1, "Blood Sugar", "Dr. Smith",
                                             TestOrder.Priority.ROUTINE, "Fasting");

        assertNotNull(order);
        assertEquals(101, order.getId());
        assertEquals(TestOrder.Status.ORDERED, order.getStatus());
        assertEquals(TestOrder.Priority.ROUTINE, order.getPriority());

        verify(orderRepo).save(any());
        verify(notificationService).send(eq(1), contains("Blood Sugar"), eq(Notification.Type.ORDER_CREATED));
    }

    @Test
    @DisplayName("placeOrder: throws if patient not found")
    void placeOrder_patientNotFound() throws Exception {
        when(patientRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.placeOrder(999, "CBC", "Dr. A", TestOrder.Priority.ROUTINE, ""));
    }

    @Test
    @DisplayName("placeOrder: throws if test name is blank")
    void placeOrder_blankTestName() throws Exception {
        when(patientRepo.findById(1)).thenReturn(Optional.of(PATIENT));

        assertThrows(IllegalArgumentException.class,
            () -> service.placeOrder(1, "  ", "Dr. A", TestOrder.Priority.ROUTINE, ""));
    }

    @Test
    @DisplayName("placeOrder: LAB_TECHNICIAN cannot place orders")
    void placeOrder_unauthorizedRole() throws Exception {
        User labTech = new User(2, "labtech1", "lt@medlab.com", "7777777777",
                                "hash", "LAB_TECHNICIAN", true, false,
                                "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(labTech);

        assertThrows(SecurityException.class,
            () -> service.placeOrder(1, "CBC", "Dr. A", TestOrder.Priority.ROUTINE, ""));
    }

    // ── cancelOrder ───────────────────────────────────────────────

    @Test
    @DisplayName("cancelOrder: cancels a PENDING order and sends notification")
    void cancelOrder_success() throws Exception {
        TestOrder order = new TestOrder(5, 1, "CBC", "Dr. B",
                                        "ORDERED", "ROUTINE", "",
                                        false, "2024-01-01 10:00:00", "2024-01-01 10:00:00", "admin");
        when(orderRepo.findById(5)).thenReturn(Optional.of(order));
        doNothing().when(orderRepo).softDelete(anyInt(), anyString());
        doNothing().when(notificationService).send(anyInt(), anyString(), any());

        assertDoesNotThrow(() -> service.cancelOrder(5));
        verify(orderRepo).softDelete(eq(5), anyString());
        verify(notificationService).send(eq(1), anyString(), eq(Notification.Type.ORDER_CANCELLED));
    }

    @Test
    @DisplayName("cancelOrder: throws if order is COMPLETED")
    void cancelOrder_completed() throws Exception {
        TestOrder order = new TestOrder(5, 1, "CBC", "Dr. B",
                                        "COMPLETED", "ROUTINE", "",
                                        false, "2024-01-01", "2024-01-01", "admin");
        when(orderRepo.findById(5)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> service.cancelOrder(5));
    }

    // ── getAllOrders ──────────────────────────────────────────────

    @Test
    @DisplayName("getAllOrders: returns list of orders")
    void getAllOrders_returnsList() throws Exception {
        List<TestOrder> mockOrders = List.of(
            new TestOrder(1, 1, "CBC", "Dr. A", "ORDERED", "ROUTINE", "",
                          false, "2024-01-01", "2024-01-01", "admin"),
            new TestOrder(2, 2, "Lipid Profile", "Dr. B", "COMPLETED", "URGENT", "",
                          false, "2024-01-02", "2024-01-02", "admin")
        );
        when(orderRepo.findAll()).thenReturn(mockOrders);

        List<TestOrder> result = service.getAllOrders();
        assertEquals(2, result.size());
        assertEquals("CBC", result.get(0).getTestName());
    }

    // ── updateStatus ─────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus: LAB_TECHNICIAN can update status")
    void updateStatus_labTech() throws Exception {
        User labTech = new User(2, "labtech1", "lt@medlab.com", "7777777777",
                                "hash", "LAB_TECHNICIAN", true, false,
                                "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(labTech);

        TestOrder order = new TestOrder(3, 1, "Thyroid", "Dr. C",
                                        "SAMPLE_COLLECTED", "ROUTINE", "",
                                        false, "2024-01-01", "2024-01-01", "admin");
        when(orderRepo.findById(3)).thenReturn(Optional.of(order));
        doNothing().when(orderRepo).updateStatus(anyInt(), any(), anyString());

        assertDoesNotThrow(() -> service.updateStatus(3, TestOrder.Status.IN_PROGRESS));
        verify(orderRepo).updateStatus(eq(3), eq(TestOrder.Status.IN_PROGRESS), anyString());
    }
}
