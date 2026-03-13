package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.*;
import com.medlab.repository.ReportRepository;
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
 * Unit tests for ReportService.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository      reportRepo;
    @Mock private TestOrderRepository   orderRepo;
    @Mock private NotificationService   notificationService;

    @InjectMocks private ReportService service;

    private static final TestOrder IN_PROGRESS_ORDER = new TestOrder(
        10, 1, "Blood Sugar", "Dr. A",
        "IN_PROGRESS", "ROUTINE", "", false,
        "2024-01-01 10:00:00", "2024-01-01 10:00:00", "admin"
    );

    @BeforeEach
    void setUpAuth() {
        User labTech = new User(2, "labtech1", "lt@medlab.com", "7777777777",
                                "hash", "LAB_TECHNICIAN", true, false,
                                "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(labTech);
    }

    @AfterEach
    void tearDown() {
        AuthContext.logout();
    }

    // ── createReport ──────────────────────────────────────────────

    @Test
    @DisplayName("createReport: creates DRAFT report, advances order to REPORT_READY")
    void createReport_success() throws SQLException {
        when(orderRepo.findById(10)).thenReturn(Optional.of(IN_PROGRESS_ORDER));
        when(reportRepo.save(any())).thenAnswer(inv -> {
            Report r = inv.getArgument(0);
            r.setId(200);
            return r;
        });
        doNothing().when(orderRepo).updateStatus(anyInt(), any(), anyString());

        Report report = service.createReport(10, "95", "Normal fasting",
                                             "70-100", "Tech Alice");

        assertNotNull(report);
        assertEquals(200, report.getId());
        assertEquals(Report.Status.DRAFT, report.getStatus());
        assertFalse(report.isAbnormal(), "95 mg/dL is within 70-100 normal range");

        verify(orderRepo).updateStatus(eq(10), eq(TestOrder.Status.REPORT_READY), anyString());
    }

    @Test
    @DisplayName("createReport: flags abnormal when value is outside normal range")
    void createReport_abnormal() throws SQLException {
        when(orderRepo.findById(10)).thenReturn(Optional.of(IN_PROGRESS_ORDER));
        when(reportRepo.save(any())).thenAnswer(inv -> {
            Report r = inv.getArgument(0);
            r.setId(201);
            return r;
        });
        doNothing().when(orderRepo).updateStatus(anyInt(), any(), anyString());

        Report report = service.createReport(10, "350", "High glucose",
                                             "70-100", "Tech Alice");

        assertTrue(report.isAbnormal(), "350 mg/dL is above the 70-100 normal range");
    }

    @Test
    @DisplayName("createReport: non-numeric result is not flagged abnormal")
    void createReport_nonNumeric_notAbnormal() throws SQLException {
        when(orderRepo.findById(10)).thenReturn(Optional.of(IN_PROGRESS_ORDER));
        when(reportRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(orderRepo).updateStatus(anyInt(), any(), anyString());

        Report report = service.createReport(10, "Negative", "HIV test result",
                                             "", "Tech Bob");

        assertFalse(report.isAbnormal());
    }

    @Test
    @DisplayName("createReport: throws if order not found")
    void createReport_orderNotFound() {
        when(orderRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.createReport(999, "5.6", "", "< 6.0", "Tech A"));
    }

    @Test
    @DisplayName("createReport: throws if order not in correct state")
    void createReport_wrongOrderState() throws SQLException {
        TestOrder orderedOrder = new TestOrder(
            12, 1, "CBC", "Dr. B", "ORDERED", "ROUTINE", "",
            false, "2024-01-01", "2024-01-01", "admin"
        );
        when(orderRepo.findById(12)).thenReturn(Optional.of(orderedOrder));

        assertThrows(IllegalStateException.class,
            () -> service.createReport(12, "Normal", "", "", "Tech A"));
    }

    // ── verifyReport ──────────────────────────────────────────────

    @Test
    @DisplayName("verifyReport: transitions DRAFT to VERIFIED")
    void verifyReport_success() throws SQLException {
        Report draft = new Report(200, 10, "95", "Normal", "70-100", false,
                                  "Tech Alice", null, "2024-01-01", "DRAFT",
                                  false, "2024-01-01", "2024-01-01", "admin");
        when(reportRepo.findById(200)).thenReturn(Optional.of(draft));
        doNothing().when(reportRepo).updateStatus(anyInt(), any(), anyString());

        assertDoesNotThrow(() -> service.verifyReport(200));
        verify(reportRepo).updateStatus(eq(200), eq(Report.Status.VERIFIED), anyString());
    }

    @Test
    @DisplayName("verifyReport: throws if report is not DRAFT")
    void verifyReport_notDraft() throws SQLException {
        Report verified = new Report(201, 10, "95", "Normal", "70-100", false,
                                     "Tech Alice", "Senior Tech", "2024-01-01", "VERIFIED",
                                     false, "2024-01-01", "2024-01-01", "admin");
        when(reportRepo.findById(201)).thenReturn(Optional.of(verified));

        assertThrows(IllegalStateException.class, () -> service.verifyReport(201));
    }

    // ── releaseReport ─────────────────────────────────────────────

    @Test
    @DisplayName("releaseReport: releases VERIFIED report and notifies patient")
    void releaseReport_success() throws SQLException {
        Report verified = new Report(202, 10, "95", "Normal", "70-100", false,
                                     "Tech Alice", "Senior Tech", "2024-01-01", "VERIFIED",
                                     false, "2024-01-01", "2024-01-01", "admin");
        when(reportRepo.findById(202)).thenReturn(Optional.of(verified));
        doNothing().when(reportRepo).updateStatus(anyInt(), any(), anyString());
        doNothing().when(orderRepo).updateStatus(anyInt(), any(), anyString());
        when(orderRepo.findById(10)).thenReturn(Optional.of(IN_PROGRESS_ORDER));
        doNothing().when(notificationService).send(anyInt(), anyString(), any());

        assertDoesNotThrow(() -> service.releaseReport(202));

        verify(orderRepo).updateStatus(eq(10), eq(TestOrder.Status.COMPLETED), anyString());
        verify(notificationService).send(eq(1), anyString(), eq(Notification.Type.REPORT_READY));
    }

    @Test
    @DisplayName("releaseReport: throws if report is not VERIFIED")
    void releaseReport_notVerified() throws SQLException {
        Report draft = new Report(203, 10, "High", "Abnormal", "70-100", true,
                                  "Tech A", null, "2024-01-01", "DRAFT",
                                  false, "2024-01-01", "2024-01-01", "admin");
        when(reportRepo.findById(203)).thenReturn(Optional.of(draft));

        assertThrows(IllegalStateException.class, () -> service.releaseReport(203));
    }

    // ── detectAbnormal unit tests ─────────────────────────────────

    @Test
    @DisplayName("detectAbnormal: correctly evaluates range comparisons")
    void detectAbnormal_rangeTests() {
        assertTrue(ReportService.detectAbnormal("350", "70-100"));   // above max
        assertTrue(ReportService.detectAbnormal("50",  "70-100"));   // below min
        assertFalse(ReportService.detectAbnormal("85",  "70-100"));  // within
        assertTrue(ReportService.detectAbnormal("6.2", "< 5.6"));    // exceeds upper bound
        assertFalse(ReportService.detectAbnormal("4.0", "< 5.6"));   // normal
        assertTrue(ReportService.detectAbnormal("1.0", "> 2.0"));    // below lower bound
        assertFalse(ReportService.detectAbnormal("Negative", "Negative")); // non-numeric
    }
}
