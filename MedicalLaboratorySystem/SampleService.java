package com.medlab.service;

import com.medlab.model.Notification;
import com.medlab.model.Sample;
import com.medlab.model.TestOrder;
import com.medlab.repository.SampleRepository;
import com.medlab.repository.TestOrderRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Sample collection and tracking.
 *
 * Future: becomes a Spring @Service; sample status events published
 *         to a Kafka topic for downstream microservices to consume.
 */
public class SampleService {

    private final SampleRepository    sampleRepo;
    private final TestOrderRepository orderRepo;
    private final NotificationService notificationService;

    public SampleService(SampleRepository sampleRepo,
                         TestOrderRepository orderRepo,
                         NotificationService notificationService) {
        this.sampleRepo          = sampleRepo;
        this.orderRepo           = orderRepo;
        this.notificationService = notificationService;
    }

    /**
     * Records that a sample has been collected for a given test order.
     * Validates the order exists and is in a PENDING state.
     * Updates the order status to SAMPLE_COLLECTED.
     */
    public Sample collectSample(int orderId, String sampleType, String collectedBy)
            throws SQLException {

        // ── Validate order ────────────────────────────────────────
        Optional<TestOrder> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Test order #" + orderId + " not found.");
        }
        TestOrder order = orderOpt.get();
        if (order.getStatus() != TestOrder.Status.PENDING) {
            throw new IllegalStateException(
                "Order #" + orderId + " is not PENDING (current: " + order.getStatus() + ").");
        }

        // ── Save sample ───────────────────────────────────────────
        String today = LocalDate.now().toString();
        Sample sample = new Sample(orderId, sampleType, collectedBy, today);
        sampleRepo.save(sample);

        // ── Advance order status ──────────────────────────────────
        orderRepo.updateStatus(orderId, TestOrder.Status.SAMPLE_COLLECTED);

        // ── Notify patient ────────────────────────────────────────
        notificationService.send(
            order.getPatientId(),
            "Your " + sampleType + " sample has been collected for order #" + orderId + ".",
            Notification.Type.SAMPLE_COLLECTED
        );

        return sample;
    }

    public void updateSampleStatus(int sampleId, Sample.Status newStatus) throws SQLException {
        Optional<Sample> existing = sampleRepo.findById(sampleId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Sample #" + sampleId + " not found.");
        }
        sampleRepo.updateStatus(sampleId, newStatus);
        System.out.println("[SampleService] Sample #" + sampleId + " status → " + newStatus);
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
