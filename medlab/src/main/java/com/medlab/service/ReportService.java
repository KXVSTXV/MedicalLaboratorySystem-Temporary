package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.*;
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
 * Includes normal-range comparison and is_abnormal flag.
 *
 * Authorization:
 *   - LAB_TECHNICIAN creates and verifies reports
 *   - ADMIN can do everything
 *   - RECEPTIONIST: read-only
 *
 * Future: Spring @Service; RELEASED event published to Notification microservice.
 */
public class ReportService {

    private final ReportRepository      reportRepo;
    private final TestOrderRepository   orderRepo;
    private final NotificationService   notificationService;

    public ReportService(ReportRepository reportRepo,
                         TestOrderRepository orderRepo,
                         NotificationService notificationService) {
        this.reportRepo          = reportRepo;
        this.orderRepo           = orderRepo;
        this.notificationService = notificationService;
    }

    /**
     * Creates a DRAFT report.
     * Order must be IN_PROGRESS.
     * Roles: LAB_TECHNICIAN, ADMIN
     */
    public Report createReport(int orderId, String result, String remarks,
                               String normalRange, String preparedBy) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.LAB_TECHNICIAN);

        Optional<TestOrder> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty())
            throw new IllegalArgumentException("Test order #" + orderId + " not found.");

        TestOrder order = orderOpt.get();
        if (order.getStatus() != TestOrder.Status.IN_PROGRESS
                && order.getStatus() != TestOrder.Status.SAMPLE_COLLECTED)
            throw new IllegalStateException(
                "Order #" + orderId + " must be IN_PROGRESS or SAMPLE_COLLECTED to create report.");

        boolean isAbnormal = detectAbnormal(result, normalRange);

        String today = LocalDate.now().toString();
        Report report = new Report(orderId, result, remarks, normalRange,
                                   isAbnormal, preparedBy.trim(), today,
                                   AuthContext.getCurrentUsername());
        reportRepo.save(report);

        // Advance order to REPORT_READY
        orderRepo.updateStatus(orderId, TestOrder.Status.REPORT_READY,
                               AuthContext.getCurrentUsername());

        if (isAbnormal) {
            System.out.println("  ⚠  ABNORMAL RESULT DETECTED for order #" + orderId);
        }

        System.out.println("[ReportService] Draft report #" + report.getId()
            + " created for order #" + orderId
            + (isAbnormal ? " [ABNORMAL]" : " [NORMAL]"));
        return report;
    }

    /**
     * Verifies a DRAFT report.
     * Roles: LAB_TECHNICIAN, ADMIN
     */
    public void verifyReport(int reportId) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.LAB_TECHNICIAN);

        Optional<Report> reportOpt = reportRepo.findById(reportId);
        if (reportOpt.isEmpty())
            throw new IllegalArgumentException("Report #" + reportId + " not found.");
        if (reportOpt.get().getStatus() != Report.Status.DRAFT)
            throw new IllegalStateException("Only DRAFT reports can be verified.");

        reportRepo.updateStatus(reportId, Report.Status.VERIFIED,
                                AuthContext.getCurrentUsername());
        System.out.println("[ReportService] Report #" + reportId + " VERIFIED by "
            + AuthContext.getCurrentUsername());
    }

    /**
     * Releases a VERIFIED report → notifies patient.
     * Roles: LAB_TECHNICIAN, ADMIN
     */
    public void releaseReport(int reportId) throws SQLException {
        AuthContext.requireRole(User.Role.ADMIN, User.Role.LAB_TECHNICIAN);

        Optional<Report> reportOpt = reportRepo.findById(reportId);
        if (reportOpt.isEmpty())
            throw new IllegalArgumentException("Report #" + reportId + " not found.");

        Report report = reportOpt.get();
        if (report.getStatus() != Report.Status.VERIFIED)
            throw new IllegalStateException("Report #" + reportId + " must be VERIFIED before releasing.");

        reportRepo.updateStatus(reportId, Report.Status.RELEASED, AuthContext.getCurrentUsername());
        orderRepo.updateStatus(report.getOrderId(), TestOrder.Status.COMPLETED,
                               AuthContext.getCurrentUsername());

        // Notify patient
        Optional<TestOrder> orderOpt = orderRepo.findById(report.getOrderId());
        orderOpt.ifPresent(o -> {
            String msg = "Your lab report #" + reportId + " is ready."
                + (report.isAbnormal() ? " ⚠ ABNORMAL values detected. Please consult your doctor." : "");
            notificationService.send(o.getPatientId(), msg, Notification.Type.REPORT_READY);
        });
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

    // ── Helpers ───────────────────────────────────────────────────

    /**
     * Simple heuristic: if a numeric result falls outside a "min-max" normal range,
     * mark it abnormal. Ranges like "70-100" or "< 5.6" are supported.
     */
    static boolean detectAbnormal(String result, String normalRange) {
        if (normalRange == null || normalRange.isBlank()) return false;
        try {
            double value = Double.parseDouble(result.trim().replaceAll("[^0-9.]", ""));
            String range = normalRange.trim();

            if (range.startsWith("<")) {
                double max = Double.parseDouble(range.substring(1).trim());
                return value >= max;
            } else if (range.startsWith(">")) {
                double min = Double.parseDouble(range.substring(1).trim());
                return value <= min;
            } else if (range.contains("-")) {
                String[] parts = range.split("-");
                double min = Double.parseDouble(parts[0].trim());
                double max = Double.parseDouble(parts[1].trim());
                return value < min || value > max;
            }
        } catch (NumberFormatException ignored) {
            // Non-numeric results (e.g. "Positive" / "Negative") — not auto-flagged
        }
        return false;
    }
}
