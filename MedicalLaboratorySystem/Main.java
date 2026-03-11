package com.medlab;

import com.medlab.db.DatabaseConnection;
import com.medlab.db.SchemaInitializer;
import com.medlab.model.*;
import com.medlab.repository.*;
import com.medlab.service.*;
import com.medlab.util.ConsoleUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════╗
 * ║   MedLab — Medical Laboratory System  v1.0  ║
 * ║   CLI Edition                               ║
 * ╚══════════════════════════════════════════════╝
 *
 * Entry point. Wires up all repositories and services (manual DI).
 *
 * Future: This wiring is replaced by Spring's IoC container.
 *         Each service block becomes its own microservice with
 *         a REST controller, and this Main.java is deleted.
 */
public class Main {

    // ── Service layer ─────────────────────────────────────────────
    private static PatientService     patientService;
    private static TestOrderService   testOrderService;
    private static SampleService      sampleService;
    private static ReportService      reportService;
    private static NotificationService notificationService;

    public static void main(String[] args) {
        printBanner();
        SchemaInitializer.initialize();
        wireServices();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = ConsoleUtil.readInt("  Enter choice: ");
            System.out.println();
            try {
                switch (choice) {
                    case 1  -> menuPatients();
                    case 2  -> menuTestOrders();
                    case 3  -> menuSamples();
                    case 4  -> menuReports();
                    case 5  -> menuNotifications();
                    case 0  -> running = false;
                    default -> ConsoleUtil.printError("Invalid option. Try again.");
                }
            } catch (Exception e) {
                ConsoleUtil.printError(e.getMessage());
                ConsoleUtil.pressEnter();
            }
        }

        System.out.println("\n  Goodbye! Closing database...");
        DatabaseConnection.close();
    }

    // ════════════════════════════════════════════════════════════
    //  WIRING  (Manual Dependency Injection)
    // ════════════════════════════════════════════════════════════

    private static void wireServices() {
        PatientRepository     patientRepo      = new PatientRepository();
        TestOrderRepository   orderRepo        = new TestOrderRepository();
        SampleRepository      sampleRepo       = new SampleRepository();
        ReportRepository      reportRepo       = new ReportRepository();
        NotificationRepository notificationRepo = new NotificationRepository();

        notificationService = new NotificationService(notificationRepo);
        patientService      = new PatientService(patientRepo);
        testOrderService    = new TestOrderService(orderRepo, patientRepo, notificationService);
        sampleService       = new SampleService(sampleRepo, orderRepo, notificationService);
        reportService       = new ReportService(reportRepo, orderRepo, notificationService);
    }

    // ════════════════════════════════════════════════════════════
    //  MENUS
    // ════════════════════════════════════════════════════════════

    private static void printMainMenu() {
        ConsoleUtil.printHeader("MAIN MENU — MedLab System");
        System.out.println("  1. Patient Management");
        System.out.println("  2. Test Order Service");
        System.out.println("  3. Sample Service");
        System.out.println("  4. Report Service");
        System.out.println("  5. Notifications");
        System.out.println("  0. Exit");
        ConsoleUtil.printDivider();
    }

    // ── 1. PATIENTS ───────────────────────────────────────────────

    private static void menuPatients() throws SQLException {
        ConsoleUtil.printHeader("PATIENT MANAGEMENT");
        System.out.println("  1. Register new patient");
        System.out.println("  2. View all patients");
        System.out.println("  3. Find patient by ID");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                String name    = ConsoleUtil.readString("  Full name    : ");
                int    age     = ConsoleUtil.readInt   ("  Age          : ");
                String gender  = ConsoleUtil.readString("  Gender (M/F/O): ");
                String contact = ConsoleUtil.readString("  Contact no.  : ");
                Patient p = patientService.registerPatient(name, age, gender, contact);
                ConsoleUtil.printSuccess("Patient registered! ID = " + p.getId());
                ConsoleUtil.pressEnter();
            }
            case 2 -> {
                List<Patient> patients = patientService.getAllPatients();
                if (patients.isEmpty()) {
                    System.out.println("  No patients registered yet.");
                } else {
                    patients.forEach(p -> System.out.println("  " + p));
                }
                ConsoleUtil.pressEnter();
            }
            case 3 -> {
                int id = ConsoleUtil.readInt("  Patient ID: ");
                patientService.findById(id).ifPresentOrElse(
                    p -> System.out.println("  " + p),
                    () -> ConsoleUtil.printError("Patient #" + id + " not found.")
                );
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 2. TEST ORDERS ────────────────────────────────────────────

    private static void menuTestOrders() throws SQLException {
        ConsoleUtil.printHeader("TEST ORDER SERVICE");
        System.out.println("  1. Place new test order");
        System.out.println("  2. View all orders");
        System.out.println("  3. View orders by patient");
        System.out.println("  4. Update order status");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                int    patientId  = ConsoleUtil.readInt   ("  Patient ID    : ");
                String testName   = ConsoleUtil.readString("  Test name     : ");
                String orderedBy  = ConsoleUtil.readString("  Ordered by (Doctor): ");
                TestOrder o = testOrderService.placeOrder(patientId, testName, orderedBy);
                ConsoleUtil.printSuccess("Order #" + o.getId() + " placed! Status: " + o.getStatus());
                ConsoleUtil.pressEnter();
            }
            case 2 -> {
                List<TestOrder> orders = testOrderService.getAllOrders();
                if (orders.isEmpty()) System.out.println("  No orders found.");
                else orders.forEach(o -> System.out.println("  " + o));
                ConsoleUtil.pressEnter();
            }
            case 3 -> {
                int pid = ConsoleUtil.readInt("  Patient ID: ");
                List<TestOrder> orders = testOrderService.getOrdersByPatient(pid);
                if (orders.isEmpty()) System.out.println("  No orders for patient #" + pid);
                else orders.forEach(o -> System.out.println("  " + o));
                ConsoleUtil.pressEnter();
            }
            case 4 -> {
                int id = ConsoleUtil.readInt("  Order ID: ");
                System.out.println("  Statuses: PENDING | SAMPLE_COLLECTED | IN_PROGRESS | COMPLETED");
                String s = ConsoleUtil.readString("  New status: ");
                testOrderService.updateStatus(id, TestOrder.Status.valueOf(s.toUpperCase()));
                ConsoleUtil.printSuccess("Order #" + id + " updated.");
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 3. SAMPLES ────────────────────────────────────────────────

    private static void menuSamples() throws SQLException {
        ConsoleUtil.printHeader("SAMPLE SERVICE");
        System.out.println("  1. Record sample collection");
        System.out.println("  2. View all samples");
        System.out.println("  3. View samples by order");
        System.out.println("  4. Update sample status");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                int    orderId     = ConsoleUtil.readInt   ("  Order ID             : ");
                String sampleType  = ConsoleUtil.readString("  Sample type (BLOOD/URINE/etc): ");
                String collectedBy = ConsoleUtil.readString("  Collected by          : ");
                Sample s = sampleService.collectSample(orderId, sampleType, collectedBy);
                ConsoleUtil.printSuccess("Sample #" + s.getId() + " recorded.");
                ConsoleUtil.pressEnter();
            }
            case 2 -> {
                List<Sample> samples = sampleService.getAllSamples();
                if (samples.isEmpty()) System.out.println("  No samples found.");
                else samples.forEach(s -> System.out.println("  " + s));
                ConsoleUtil.pressEnter();
            }
            case 3 -> {
                int oid = ConsoleUtil.readInt("  Order ID: ");
                List<Sample> samples = sampleService.getSamplesByOrder(oid);
                if (samples.isEmpty()) System.out.println("  No samples for order #" + oid);
                else samples.forEach(s -> System.out.println("  " + s));
                ConsoleUtil.pressEnter();
            }
            case 4 -> {
                int id = ConsoleUtil.readInt("  Sample ID: ");
                System.out.println("  Statuses: COLLECTED | PROCESSING | ANALYSED");
                String s = ConsoleUtil.readString("  New status: ");
                sampleService.updateSampleStatus(id, Sample.Status.valueOf(s.toUpperCase()));
                ConsoleUtil.printSuccess("Sample #" + id + " updated.");
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 4. REPORTS ────────────────────────────────────────────────

    private static void menuReports() throws SQLException {
        ConsoleUtil.printHeader("REPORT SERVICE");
        System.out.println("  1. Create draft report");
        System.out.println("  2. Verify report");
        System.out.println("  3. Release report (notifies patient)");
        System.out.println("  4. View all reports");
        System.out.println("  5. View reports by order");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                int    orderId    = ConsoleUtil.readInt   ("  Order ID    : ");
                String result     = ConsoleUtil.readString("  Result      : ");
                String remarks    = ConsoleUtil.readString("  Remarks     : ");
                String preparedBy = ConsoleUtil.readString("  Prepared by : ");
                Report r = reportService.createReport(orderId, result, remarks, preparedBy);
                ConsoleUtil.printSuccess("Draft report #" + r.getId() + " created.");
                ConsoleUtil.pressEnter();
            }
            case 2 -> {
                int id = ConsoleUtil.readInt("  Report ID to verify: ");
                reportService.verifyReport(id);
                ConsoleUtil.printSuccess("Report #" + id + " verified.");
                ConsoleUtil.pressEnter();
            }
            case 3 -> {
                int id = ConsoleUtil.readInt("  Report ID to release: ");
                reportService.releaseReport(id);
                ConsoleUtil.printSuccess("Report #" + id + " released. Patient notified.");
                ConsoleUtil.pressEnter();
            }
            case 4 -> {
                List<Report> reports = reportService.getAllReports();
                if (reports.isEmpty()) System.out.println("  No reports found.");
                else reports.forEach(r -> System.out.println("  " + r));
                ConsoleUtil.pressEnter();
            }
            case 5 -> {
                int oid = ConsoleUtil.readInt("  Order ID: ");
                List<Report> reports = reportService.getReportsByOrder(oid);
                if (reports.isEmpty()) System.out.println("  No reports for order #" + oid);
                else reports.forEach(r -> System.out.println("  " + r));
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 5. NOTIFICATIONS ──────────────────────────────────────────

    private static void menuNotifications() throws SQLException {
        ConsoleUtil.printHeader("NOTIFICATION LOG");
        System.out.println("  1. View all notifications");
        System.out.println("  2. View notifications for a patient");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                List<Notification> all = notificationService.getAllNotifications();
                if (all.isEmpty()) System.out.println("  No notifications yet.");
                else all.forEach(n -> System.out.println("  " + n));
                ConsoleUtil.pressEnter();
            }
            case 2 -> {
                int pid = ConsoleUtil.readInt("  Patient ID: ");
                List<Notification> list = notificationService.getNotificationsForPatient(pid);
                if (list.isEmpty()) System.out.println("  No notifications for patient #" + pid);
                else list.forEach(n -> System.out.println("  " + n));
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── Banner ────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println("""
            
            ╔══════════════════════════════════════════════════╗
            ║                                                  ║
            ║   🔬  MedLab — Medical Laboratory System  🔬     ║
            ║            CLI Edition  v1.0                     ║
            ║                                                  ║
            ║   Services:                                      ║
            ║     • Test Order Service                         ║
            ║     • Sample Service                             ║
            ║     • Report Service                             ║
            ║     • Notification Service                       ║
            ║                                                  ║
            ╚══════════════════════════════════════════════════╝
            """);
    }
}
