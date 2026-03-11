package com.medlab.service;

import com.medlab.model.Notification;
import com.medlab.model.Report;
import com.medlab.model.TestOrder;
import com.medlab.repository.ReportRepository;
import com.medlab.repository.TestOrderRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Lab Report creation and lifecycle.
 *
 * Lifecycle: DRAFT → VERIFIED → RELEASED
 *
 * Future: becomes a Spring @Service; on RELEASED, an event is published
 *         to the Notification microservice via a message broker.
 */
public class ReportService {

    private final ReportRepository    reportRepo;
    private final TestOrderRepository orderRepo;
    private final NotificationService notificationService;

    public ReportService(ReportRepository reportRepo,
                         TestOrderRepository orderRepo,
                         NotificationService notificationService) {
        this.reportRepo          = reportRepo;
        this.orderRepo           = orderRepo;
        this.notificationService = notificationService;
    }

    /**
     * Creates a draft report for a given order.
     * Order must be in SAMPLE_COLLECTED or IN_PROGRESS state.
     */
    public Report createReport(int orderId, String result, String remarks, String preparedBy)
            throws SQLException {

        Optional<TestOrder> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Test order #" + orderId + " not found.");
        }
        TestOrder order = orderOpt.get();

        String today = LocalDate.now().toString();
        Report report = new Report(orderId, result, remarks, preparedBy, today);
        reportRepo.save(report);

        // Advance order to IN_PROGRESS if it was SAMPLE_COLLECTED
        if (order.getStatus() == TestOrder.Status.SAMPLE_COLLECTED) {
            orderRepo.updateStatus(orderId, TestOrder.Status.IN_PROGRESS);
        }

        System.out.println("[ReportService] Draft report #" + report.getId()
            + " created for order #" + orderId);
        return report;
    }

    /**
     * Releases (finalises) a report.
     * Must be VERIFIED before releasing.
     * On release: order status → COMPLETED, patient notified.
     */
    public void releaseReport(int reportId) throws SQLException {
        Optional<Report> reportOpt = reportRepo.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Report #" + reportId + " not found.");
        }
        Report report = reportOpt.get();
        if (report.getStatus() != Report.Status.VERIFIED) {
            throw new IllegalStateException(
                "Report #" + reportId + " must be VERIFIED before releasing.");
        }

        reportRepo.updateStatus(reportId, Report.Status.RELEASED);
        orderRepo.updateStatus(report.getOrderId(), TestOrder.Status.COMPLETED);

        // Fetch patient ID via the order
        Optional<TestOrder> order = orderRepo.findById(report.getOrderId());
        order.ifPresent(o -> notificationService.send(
            o.getPatientId(),
            "Your lab report #" + reportId + " is ready. Please collect it.",
            Notification.Type.REPORT_READY
        ));
    }

    public void verifyReport(int reportId) throws SQLException {
        Optional<Report> reportOpt = reportRepo.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Report #" + reportId + " not found.");
        }
        if (reportOpt.get().getStatus() != Report.Status.DRAFT) {
            throw new IllegalStateException("Only DRAFT reports can be verified.");
        }
        reportRepo.updateStatus(reportId, Report.Status.VERIFIED);
        System.out.println("[ReportService] Report #" + reportId + " VERIFIED.");
    }

    public Optional<Report> getReportById(int id) throws SQLException {
        return reportRepo.findById(id);
    }

    public List<Report> getAllReports() throws SQLException {
        return reportRepo.findAll();
    }

    public List<Report> getReportsByOrder(int orderId) throws SQLException {
        return reportRepo.findByOrderId(orderId);
    }
}
