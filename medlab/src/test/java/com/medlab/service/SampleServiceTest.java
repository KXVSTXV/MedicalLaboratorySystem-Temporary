package com.medlab.service;

import com.medlab.auth.AuthContext;
import com.medlab.model.*;
import com.medlab.repository.SampleRepository;
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
 * Unit tests for SampleService.
 */
@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock private SampleRepository      sampleRepo;
    @Mock private TestOrderRepository   orderRepo;
    @Mock private NotificationService   notificationService;

    @InjectMocks private SampleService service;

    private static final TestOrder ORDERED_ORDER = new TestOrder(
        10, 1, "Blood Sugar", "Dr. A",
        "ORDERED", "ROUTINE", "", false,
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

    // ── collectSample ─────────────────────────────────────────────

    @Test
    @DisplayName("collectSample: happy path — records sample and advances order")
    void collectSample_success() throws SQLException {
        when(orderRepo.findById(10)).thenReturn(Optional.of(ORDERED_ORDER));
        when(sampleRepo.save(any())).thenAnswer(inv -> {
            Sample s = inv.getArgument(0);
            s.setId(50);
            return s;
        });
        doNothing().when(orderRepo).updateStatus(anyInt(), any(), anyString());
        doNothing().when(notificationService).send(anyInt(), anyString(), any());

        Sample s = service.collectSample(10, "BLOOD", "Tech Alice");

        assertNotNull(s);
        assertEquals(50, s.getId());
        assertEquals(Sample.Status.COLLECTED, s.getStatus());
        assertEquals(Sample.SampleType.BLOOD, s.getSampleType());

        verify(orderRepo).updateStatus(eq(10), eq(TestOrder.Status.SAMPLE_COLLECTED), anyString());
        verify(notificationService).send(eq(1), contains("BLOOD"), eq(Notification.Type.SAMPLE_COLLECTED));
    }

    @Test
    @DisplayName("collectSample: throws if order not found")
    void collectSample_orderNotFound() {
        when(orderRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.collectSample(999, "BLOOD", "Tech Alice"));
    }

    @Test
    @DisplayName("collectSample: throws if order is not in ORDERED state")
    void collectSample_wrongStatus() throws SQLException {
        TestOrder inProgress = new TestOrder(
            11, 1, "CBC", "Dr. B",
            "IN_PROGRESS", "ROUTINE", "", false,
            "2024-01-01", "2024-01-01", "admin"
        );
        when(orderRepo.findById(11)).thenReturn(Optional.of(inProgress));

        assertThrows(IllegalStateException.class,
            () -> service.collectSample(11, "BLOOD", "Tech Bob"));
    }

    @Test
    @DisplayName("collectSample: RECEPTIONIST cannot collect samples")
    void collectSample_unauthorizedRole() {
        User receptionist = new User(3, "rec1", "r@medlab.com", "8888888888",
                                     "hash", "RECEPTIONIST", true, false,
                                     "2024-01-01", "2024-01-01", "SYSTEM");
        AuthContext.setCurrentUser(receptionist);

        assertThrows(SecurityException.class,
            () -> service.collectSample(10, "BLOOD", "Tech Alice"));
    }

    // ── updateSampleStatus ────────────────────────────────────────

    @Test
    @DisplayName("updateSampleStatus: ANALYSED advances order to IN_PROGRESS")
    void updateStatus_analysed_advancesOrder() throws SQLException {
        Sample sample = new Sample(50, 10, "BLOOD", "Tech A",
                                   "2024-01-01", "COLLECTED", null,
                                   false, "2024-01-01", "2024-01-01", "admin");
        when(sampleRepo.findById(50)).thenReturn(Optional.of(sample));
        doNothing().when(sampleRepo).updateStatus(anyInt(), any(), any());

        TestOrder order = new TestOrder(10, 1, "CBC", "Dr. A",
                                        "SAMPLE_COLLECTED", "ROUTINE", "",
                                        false, "2024-01-01", "2024-01-01", "admin");
        when(orderRepo.findById(10)).thenReturn(Optional.of(order));
        doNothing().when(orderRepo).updateStatus(anyInt(), any(), anyString());

        assertDoesNotThrow(() -> service.updateSampleStatus(50, Sample.Status.ANALYSED, null));
        verify(sampleRepo).updateStatus(eq(50), eq(Sample.Status.ANALYSED), any());
        verify(orderRepo).updateStatus(eq(10), eq(TestOrder.Status.IN_PROGRESS), anyString());
    }

    @Test
    @DisplayName("updateSampleStatus: REJECTED records rejection reason")
    void updateStatus_rejected() throws SQLException {
        Sample sample = new Sample(51, 11, "URINE", "Tech B",
                                   "2024-01-01", "COLLECTED", null,
                                   false, "2024-01-01", "2024-01-01", "admin");
        when(sampleRepo.findById(51)).thenReturn(Optional.of(sample));
        doNothing().when(sampleRepo).updateStatus(anyInt(), any(), anyString());

        assertDoesNotThrow(() ->
            service.updateSampleStatus(51, Sample.Status.REJECTED, "Hemolyzed sample"));

        verify(sampleRepo).updateStatus(eq(51), eq(Sample.Status.REJECTED), eq("Hemolyzed sample"));
    }

    // ── getAllSamples ─────────────────────────────────────────────

    @Test
    @DisplayName("getAllSamples: returns non-deleted samples")
    void getAllSamples() throws SQLException {
        when(sampleRepo.findAll()).thenReturn(List.of(
            new Sample(1, 10, "BLOOD", "Tech A", "2024-01-01", "COLLECTED",
                       null, false, "2024-01-01", "2024-01-01", "admin"),
            new Sample(2, 11, "URINE", "Tech B", "2024-01-02", "ANALYSED",
                       null, false, "2024-01-02", "2024-01-02", "admin")
        ));

        List<Sample> result = service.getAllSamples();
        assertEquals(2, result.size());
    }
}
