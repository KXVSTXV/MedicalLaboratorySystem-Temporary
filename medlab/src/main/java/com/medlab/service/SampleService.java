package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.*;
import com.medlab.repository.SampleRepository;
import com.medlab.repository.TestOrderRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Sample collection and tracking.
 *
 * Authorization:
 *   - LAB_TECHNICIAN / ADMIN can collect and update samples.
 *   - RECEPTIONIST has read-only access.
 *
 * Future: Spring @Service; sample status events published to Kafka.
 */
public class SampleService {

    private final SampleRepository      sampleRepo;
    private final TestOrderRepository   orderRepo;
    private final NotificationService   notificationService;

    public SampleService(SampleRepository sampleRepo,
                         TestOrderRepository orderRepo,
                         NotificationService notificationService) {
        this.sampleRepo          = sampleRepo;
        this.orderRepo           = orderRepo;
        this.notificationService = notificationService;
    }

    /**
     * Records that a sample has been collected.
     * Order must be in ORDERED state.
     * Advances order status to SAMPLE_COLLECTED.
     *
     * Roles: LAB_TECHNICIAN, ADMIN
     */
    public Sample collectSample(int orderId, String sampleType, String collectedBy)
            throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.LAB_TECHNICIAN);

        Optional<TestOrder> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty())
            throw new IllegalArgumentException("Order #" + orderId + " not found.");

        TestOrder order = orderOpt.get();
        if (order.getStatus() != TestOrder.Status.ORDERED)
            throw new IllegalStateException(
                "Order #" + orderId + " is not in ORDERED state (current: " + order.getStatus() + ").");

        String today  = LocalDate.now().toString();
        Sample sample = new Sample(orderId, sampleType.toUpperCase(),
                                   collectedBy.trim(), today,
                                   AuthContext.getCurrentUsername());
        sampleRepo.save(sample);

        orderRepo.updateStatus(orderId, TestOrder.Status.SAMPLE_COLLECTED,
                               AuthContext.getCurrentUsername());

        notificationService.send(
            order.getPatientId(),
            "Your " + sampleType + " sample has been collected for order #" + orderId + ".",
            Notification.Type.SAMPLE_COLLECTED
        );

        return sample;
    }

    /**
     * Updates sample status (PROCESSING / ANALYSED / REJECTED).
     * Roles: LAB_TECHNICIAN, ADMIN
     */
    public void updateSampleStatus(int sampleId, Sample.Status newStatus,
                                   String rejectionReason) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.LAB_TECHNICIAN);

        Optional<Sample> existing = sampleRepo.findById(sampleId);
        if (existing.isEmpty())
            throw new IllegalArgumentException("Sample #" + sampleId + " not found.");

        sampleRepo.updateStatus(sampleId, newStatus, rejectionReason);
        System.out.println("[SampleService] Sample #" + sampleId + " status → " + newStatus);

        // If analysed, advance the order to IN_PROGRESS
        if (newStatus == Sample.Status.ANALYSED) {
            Sample s = existing.get();
            orderRepo.findById(s.getOrderId()).ifPresent(order -> {
                try {
                    if (order.getStatus() == TestOrder.Status.SAMPLE_COLLECTED) {
                        orderRepo.updateStatus(order.getId(), TestOrder.Status.IN_PROGRESS,
                                               AuthContext.getCurrentUsername());
                    }
                } catch (SQLException e) {
                    System.err.println("[SampleService] Could not advance order status: " + e.getMessage());
                }
            });
        }
    }

    public Optional<Sample> getSampleById(int id) throws SQLException {
        return sampleRepo.findById(id);
    }

    public List<Sample> getAllSamples() throws SQLException {
        return sampleRepo.findAll();
    }

    public List<Sample> getSamplesByOrder(int orderId) throws SQLException {
        return sampleRepo.findByOrderId(orderId);
    }
}
