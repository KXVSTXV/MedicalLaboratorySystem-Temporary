package com.medlab.service;

import com.medlab.model.Notification;
import com.medlab.model.Patient;
import com.medlab.model.TestOrder;
import com.medlab.repository.PatientRepository;
import com.medlab.repository.TestOrderRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Test Order management.
 *
 * Future: becomes a Spring @Service injected via @Autowired / constructor injection.
 *         Validation becomes @Valid + Bean Validation annotations on the model.
 */
public class TestOrderService {

    private final TestOrderRepository orderRepo;
    private final PatientRepository   patientRepo;
    private final NotificationService notificationService;

    public TestOrderService(TestOrderRepository orderRepo,
                            PatientRepository patientRepo,
                            NotificationService notificationService) {
        this.orderRepo           = orderRepo;
        this.patientRepo         = patientRepo;
        this.notificationService = notificationService;
    }

    /**
     * Places a new test order for a patient.
     * Validates patient exists, saves the order, fires a notification.
     */
    public TestOrder placeOrder(int patientId, String testName, String orderedBy)
            throws SQLException {

        // ── Validate patient exists ───────────────────────────────
        Optional<Patient> patient = patientRepo.findById(patientId);
        if (patient.isEmpty()) {
            throw new IllegalArgumentException("Patient with ID " + patientId + " not found.");
        }

        String today = LocalDate.now().toString();
        TestOrder order = new TestOrder(patientId, testName, orderedBy, today);
        orderRepo.save(order);

        // ── Trigger notification ──────────────────────────────────
        notificationService.send(
            patientId,
            "Test order #" + order.getId() + " for '" + testName + "' has been created.",
            Notification.Type.ORDER_CREATED
        );

        return order;
    }

    public Optional<TestOrder> getOrderById(int id) throws SQLException {
        return orderRepo.findById(id);
    }

    public List<TestOrder> getAllOrders() throws SQLException {
        return orderRepo.findAll();
    }

    public List<TestOrder> getOrdersByPatient(int patientId) throws SQLException {
        return orderRepo.findByPatientId(patientId);
    }

    public void updateStatus(int orderId, TestOrder.Status newStatus) throws SQLException {
        Optional<TestOrder> existing = orderRepo.findById(orderId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Test order #" + orderId + " not found.");
        }
        orderRepo.updateStatus(orderId, newStatus);
        System.out.println("[TestOrderService] Order #" + orderId + " status → " + newStatus);
    }
}
