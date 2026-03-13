package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.*;
import com.medlab.repository.PatientRepository;
import com.medlab.repository.TestOrderRepository;
import com.medlab.util.PerformanceMetrics;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Test Order management.
 *
 * Authorization:
 *   - RECEPTIONIST / ADMIN  → place, view, cancel orders
 *   - LAB_TECHNICIAN        → view and update status only
 *
 * Future: Spring @Service; validation via Bean Validation annotations.
 */
public class TestOrderService {

    private final TestOrderRepository   orderRepo;
    private final PatientRepository     patientRepo;
    private final NotificationService   notificationService;

    public TestOrderService(TestOrderRepository orderRepo,
                            PatientRepository patientRepo,
                            NotificationService notificationService) {
        this.orderRepo           = orderRepo;
        this.patientRepo         = patientRepo;
        this.notificationService = notificationService;
    }

    /**
     * Places a new test order.
     * Roles allowed: ADMIN, RECEPTIONIST
     */
    public TestOrder placeOrder(int patientId, String testName, String orderedBy,
                                TestOrder.Priority priority, String notes) throws Exception {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.RECEPTIONIST);

        if (testName == null || testName.isBlank())
            throw new IllegalArgumentException("Test name cannot be empty.");

        Optional<Patient> patient = patientRepo.findById(patientId);
        if (patient.isEmpty())
            throw new IllegalArgumentException("Patient #" + patientId + " not found.");

        TestOrder order = new TestOrder(patientId, testName.trim(), orderedBy.trim(),
                                        priority, notes, AuthContext.getCurrentUsername());

        TestOrder saved = PerformanceMetrics.measure(
            "TestOrderService.placeOrder",
            () -> orderRepo.save(order)
        );

        notificationService.send(
            patientId,
            "Test order #" + saved.getId() + " for '" + testName + "' has been placed. Priority: " + priority,
            Notification.Type.ORDER_CREATED
        );

        return saved;
    }

    /**
     * Cancels an order (soft-delete).
     * Roles allowed: ADMIN, RECEPTIONIST
     */
    public void cancelOrder(int orderId) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.RECEPTIONIST);

        Optional<TestOrder> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty())
            throw new IllegalArgumentException("Order #" + orderId + " not found.");

        TestOrder order = orderOpt.get();
        if (order.getStatus() == TestOrder.Status.COMPLETED)
            throw new IllegalStateException("Cannot cancel a COMPLETED order.");

        orderRepo.softDelete(orderId, AuthContext.getCurrentUsername());

        notificationService.send(
            order.getPatientId(),
            "Your test order #" + orderId + " for '" + order.getTestName() + "' has been cancelled.",
            Notification.Type.ORDER_CANCELLED
        );
    }

    /**
     * Updates order status.
     * Roles: ADMIN, LAB_TECHNICIAN
     */
    public void updateStatus(int orderId, TestOrder.Status newStatus) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.LAB_TECHNICIAN, User.Role.RECEPTIONIST);

        Optional<TestOrder> existing = orderRepo.findById(orderId);
        if (existing.isEmpty())
            throw new IllegalArgumentException("Order #" + orderId + " not found.");

        orderRepo.updateStatus(orderId, newStatus, AuthContext.getCurrentUsername());
        System.out.println("[TestOrderService] Order #" + orderId + " status → " + newStatus);
    }

    public Optional<TestOrder> getOrderById(int id) throws SQLException {
        return orderRepo.findById(id);
    }

    public List<TestOrder> getAllOrders() throws Exception {
        return PerformanceMetrics.measure("TestOrderService.getAllOrders", orderRepo::findAll);
    }

    public List<TestOrder> getOrdersByPatient(int patientId) throws SQLException {
        return orderRepo.findByPatientId(patientId);
    }
}
