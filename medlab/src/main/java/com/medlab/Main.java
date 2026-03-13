package com.medlab;

import com.medlab.auth.AuthContext;
import com.medlab.db.DatabaseConnection;
import com.medlab.db.SchemaInitializer;
import com.medlab.model.*;
import com.medlab.repository.*;
import com.medlab.service.*;
import com.medlab.util.ConsoleUtil;
import com.medlab.util.PerformanceMetrics;

import java.util.List;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════╗
 * ║   MedLab — Medical Laboratory System  v1.0  ║
 * ║   CLI Edition  |  MySQL + JDBC              ║
 * ╚══════════════════════════════════════════════╝
 *
 * Entry point. Manual Dependency Injection wiring.
 *
 * Future (Spring Boot v2): Replaced by @SpringBootApplication + IoC container.
 */
public class Main {

    // ── Service layer ─────────────────────────────────────────────
    private static AuthService         authService;
    private static PatientService      patientService;
    private static TestOrderService    testOrderService;
    private static SampleService       sampleService;
    private static ReportService       reportService;
    private static NotificationService notificationService;

    public static void main(String[] args) {
        printBanner();
        SchemaInitializer.initialize();
        wireServices();

        // ── Authentication gate ───────────────────────────────────
        if (!loginLoop()) {
            System.out.println("\n  Exiting. Goodbye!");
            DatabaseConnection.close();
            return;
        }

        // ── Main menu loop ────────────────────────────────────────
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
                    case 6  -> menuUserAdmin();
                    case 7  -> { PerformanceMetrics.printSummary(); ConsoleUtil.pressEnter(); }
                    case 0  -> {
                        authService.logout();
                        running = false;
                    }
                    default -> ConsoleUtil.printError("Invalid option. Try again.");
                }
            } catch (SecurityException e) {
                ConsoleUtil.printError("Access denied: " + e.getMessage());
                ConsoleUtil.pressEnter();
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
        UserRepository         userRepo         = new UserRepository();
        PatientRepository      patientRepo      = new PatientRepository();
        TestOrderRepository    orderRepo        = new TestOrderRepository();
        SampleRepository       sampleRepo       = new SampleRepository();
        ReportRepository       reportRepo       = new ReportRepository();
        NotificationRepository notificationRepo = new NotificationRepository();

        notificationService = new NotificationService(notificationRepo);
        authService         = new AuthService(userRepo);
        patientService      = new PatientService(patientRepo);
        testOrderService    = new TestOrderService(orderRepo, patientRepo, notificationService);
        sampleService       = new SampleService(sampleRepo, orderRepo, notificationService);
        reportService       = new ReportService(reportRepo, orderRepo, notificationService);
    }

    // ════════════════════════════════════════════════════════════
    //  AUTHENTICATION
    // ════════════════════════════════════════════════════════════

    private static boolean loginLoop() {
        for (int attempts = 0; attempts < 3; attempts++) {
            ConsoleUtil.printHeader("LOGIN — MedLab System");
            System.out.println("  Default credentials: admin / Admin@123");
            ConsoleUtil.printDivider();
            String username = ConsoleUtil.readString("  Username : ");
            String password = ConsoleUtil.readPassword("  Password : ");

            try {
                authService.login(username, password);
                return true;
            } catch (Exception e) {
                ConsoleUtil.printError(e.getMessage());
                if (attempts < 2) System.out.println("  Attempts remaining: " + (2 - attempts));
            }
        }
        ConsoleUtil.printError("Too many failed attempts. Exiting.");
        return false;
    }

    // ════════════════════════════════════════════════════════════
    //  MENUS
    // ════════════════════════════════════════════════════════════

    private static void printMainMenu() {
        User u = AuthContext.getCurrentUser();
        ConsoleUtil.printHeader("MAIN MENU  [" + u.getUsername() + " | " + u.getRole() + "]");
        System.out.println("  1. Patient Management");
        System.out.println("  2. Test Order Service");
        System.out.println("  3. Sample Service");
        System.out.println("  4. Report Service");
        System.out.println("  5. Notifications");
        System.out.println("  6. User Administration  (ADMIN only)");
        System.out.println("  7. Performance Summary");
        System.out.println("  0. Logout & Exit");
        ConsoleUtil.printDivider();
    }

    // ── 1. PATIENTS ───────────────────────────────────────────────

    private static void menuPatients() throws Exception {
        ConsoleUtil.printHeader("PATIENT MANAGEMENT");
        System.out.println("  1. Register new patient");
        System.out.println("  2. View all patients");
        System.out.println("  3. Find patient by ID");
        System.out.println("  4. Search patients by name");
        System.out.println("  5. Soft-delete patient  (ADMIN only)");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                String name   = ConsoleUtil.readString("  Full name      : ");
                int    age    = ConsoleUtil.readInt   ("  Age            : ");
                String gender = ConsoleUtil.readString("  Gender (M/F/O) : ");
                String email  = ConsoleUtil.readString("  Email          : ");
                String mobile = ConsoleUtil.readString("  Mobile number  : ");
                String addr   = ConsoleUtil.readString("  Address        : ");
                Patient p = patientService.registerPatient(name, age, gender, email, mobile, addr);
                ConsoleUtil.printSuccess("Patient registered! ID = " + p.getId());
                ConsoleUtil.pressEnter();
            }
            case 2 -> {
                List<Patient> patients = patientService.getAllPatients();
                if (patients.isEmpty()) System.out.println("  No patients registered yet.");
                else patients.forEach(p -> System.out.println("  " + p));
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
            case 4 -> {
                String name = ConsoleUtil.readString("  Search name: ");
                List<Patient> found = patientService.searchByName(name);
                if (found.isEmpty()) System.out.println("  No patients found matching: " + name);
                else found.forEach(p -> System.out.println("  " + p));
                ConsoleUtil.pressEnter();
            }
            case 5 -> {
                int id = ConsoleUtil.readInt("  Patient ID to delete: ");
                patientService.deletePatient(id);
                ConsoleUtil.printSuccess("Patient #" + id + " soft-deleted.");
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 2. TEST ORDERS ────────────────────────────────────────────

    private static void menuTestOrders() throws Exception {
        ConsoleUtil.printHeader("TEST ORDER SERVICE");
        System.out.println("  1. Place new test order");
        System.out.println("  2. View all orders");
        System.out.println("  3. View orders by patient");
        System.out.println("  4. Update order status");
        System.out.println("  5. Cancel order (soft-delete)");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                int    patientId = ConsoleUtil.readInt   ("  Patient ID          : ");
                String testName  = ConsoleUtil.readString("  Test name            : ");
                String orderedBy = ConsoleUtil.readString("  Ordered by (Doctor)  : ");
                System.out.println("  Priority: ROUTINE | URGENT | STAT");
                String prio      = ConsoleUtil.readString("  Priority             : ");
                String notes     = ConsoleUtil.readString("  Notes (optional)     : ");
                TestOrder.Priority priority;
                try {
                    priority = TestOrder.Priority.valueOf(prio.toUpperCase());
                } catch (IllegalArgumentException e) {
                    priority = TestOrder.Priority.ROUTINE;
                }
                TestOrder o = testOrderService.placeOrder(patientId, testName, orderedBy, priority, notes);
                ConsoleUtil.printSuccess("Order #" + o.getId() + " placed! Status: " + o.getStatus()
                    + "  Priority: " + o.getPriority());
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
                System.out.println("  Statuses: ORDERED | SAMPLE_COLLECTED | IN_PROGRESS | REPORT_READY | COMPLETED");
                String s = ConsoleUtil.readString("  New status: ");
                testOrderService.updateStatus(id, TestOrder.Status.valueOf(s.toUpperCase()));
                ConsoleUtil.printSuccess("Order #" + id + " updated.");
                ConsoleUtil.pressEnter();
            }
            case 5 -> {
                int id = ConsoleUtil.readInt("  Order ID to cancel: ");
                testOrderService.cancelOrder(id);
                ConsoleUtil.printSuccess("Order #" + id + " cancelled.");
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 3. SAMPLES ────────────────────────────────────────────────

    private static void menuSamples() throws Exception {
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
                int    orderId    = ConsoleUtil.readInt   ("  Order ID                    : ");
                System.out.println("  Types: BLOOD | URINE | STOOL | SWAB | SPUTUM | CSF | OTHER");
                String sampleType = ConsoleUtil.readString("  Sample type                 : ");
                String collBy     = ConsoleUtil.readString("  Collected by (Technician)   : ");
                Sample s = sampleService.collectSample(orderId, sampleType, collBy);
                ConsoleUtil.printSuccess("Sample #" + s.getId() + " recorded. Date: " + s.getCollectedDate());
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
                System.out.println("  Statuses: COLLECTED | PROCESSING | ANALYSED | REJECTED");
                String s = ConsoleUtil.readString("  New status: ");
                String reason = "";
                if (s.equalsIgnoreCase("REJECTED")) {
                    reason = ConsoleUtil.readString("  Rejection reason: ");
                }
                sampleService.updateSampleStatus(id, Sample.Status.valueOf(s.toUpperCase()), reason);
                ConsoleUtil.printSuccess("Sample #" + id + " updated to " + s.toUpperCase());
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 4. REPORTS ────────────────────────────────────────────────

    private static void menuReports() throws Exception {
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
                int    orderId    = ConsoleUtil.readInt   ("  Order ID       : ");
                String result     = ConsoleUtil.readString("  Result value   : ");
                String normalRange= ConsoleUtil.readString("  Normal range   : ");
                String remarks    = ConsoleUtil.readString("  Remarks        : ");
                String preparedBy = ConsoleUtil.readString("  Prepared by    : ");
                Report r = reportService.createReport(orderId, result, remarks, normalRange, preparedBy);
                ConsoleUtil.printSuccess("Draft report #" + r.getId() + " created."
                    + (r.isAbnormal() ? "  ⚠ ABNORMAL!" : "  ✔ NORMAL"));
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

    private static void menuNotifications() throws Exception {
        ConsoleUtil.printHeader("NOTIFICATION LOG");
        System.out.println("  1. View all notifications");
        System.out.println("  2. View notifications for a patient");
        System.out.println("  3. View unread notifications for a patient");
        System.out.println("  4. Mark notification as read");
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
            case 3 -> {
                int pid = ConsoleUtil.readInt("  Patient ID: ");
                List<Notification> list = notificationService.getUnreadForPatient(pid);
                if (list.isEmpty()) System.out.println("  No unread notifications for patient #" + pid);
                else list.forEach(n -> System.out.println("  " + n));
                ConsoleUtil.pressEnter();
            }
            case 4 -> {
                int nid = ConsoleUtil.readInt("  Notification ID to mark as read: ");
                notificationService.markAsRead(nid);
                ConsoleUtil.printSuccess("Notification #" + nid + " marked as read.");
                ConsoleUtil.pressEnter();
            }
            case 0 -> {}
            default -> ConsoleUtil.printError("Invalid option.");
        }
    }

    // ── 6. USER ADMIN ─────────────────────────────────────────────

    private static void menuUserAdmin() throws Exception {
        ConsoleUtil.printHeader("USER ADMINISTRATION  [ADMIN ONLY]");
        System.out.println("  1. Register new user");
        System.out.println("  2. View all users");
        System.out.println("  3. Soft-delete user");
        System.out.println("  0. Back");
        ConsoleUtil.printDivider();
        int ch = ConsoleUtil.readInt("  Choice: ");

        switch (ch) {
            case 1 -> {
                String username = ConsoleUtil.readString("  Username      : ");
                String email    = ConsoleUtil.readString("  Email         : ");
                String mobile   = ConsoleUtil.readString("  Mobile number : ");
                String password = ConsoleUtil.readPassword("  Password      : ");
                System.out.println("  Roles: ADMIN | RECEPTIONIST | LAB_TECHNICIAN");
                String roleStr  = ConsoleUtil.readString("  Role          : ");
                User.Role role;
                try {
                    role = User.Role.valueOf(roleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    role = User.Role.RECEPTIONIST;
                    ConsoleUtil.printWarning("Invalid role, defaulting to RECEPTIONIST.");
                }
                User u = authService.registerUser(username, email, mobile, password, role);
                ConsoleUtil.printSuccess("User registered! ID = " + u.getId() + "  Role: " + u.getRole());
                ConsoleUtil.pressEnter();
            }
            case 2 -> {
                List<User> users = authService.getAllUsers();
                if (users.isEmpty()) System.out.println("  No users found.");
                else users.forEach(u -> System.out.println("  " + u));
                ConsoleUtil.pressEnter();
            }
            case 3 -> {
                int id = ConsoleUtil.readInt("  User ID to delete: ");
                authService.softDeleteUser(id);
                ConsoleUtil.printSuccess("User #" + id + " soft-deleted.");
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
            ║        CLI Edition  v1.0  |  MySQL + JDBC        ║
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
