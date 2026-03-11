package com.medlab.util;

import java.util.Scanner;

/**
 * Shared console utility — input reading and display helpers.
 */
public class ConsoleUtil {

    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleUtil() {}

    // ── Input ─────────────────────────────────────────────────────

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Please enter a valid number.");
            }
        }
    }

    // ── Display ───────────────────────────────────────────────────

    public static void printHeader(String title) {
        String bar = "═".repeat(50);
        System.out.println("\n╔" + bar + "╗");
        System.out.printf( "║  %-48s║%n", title);
        System.out.println("╚" + bar + "╝");
    }

    public static void printDivider() {
        System.out.println("─".repeat(52));
    }

    public static void printSuccess(String msg) {
        System.out.println("  ✔ " + msg);
    }

    public static void printError(String msg) {
        System.out.println("  ✗ ERROR: " + msg);
    }

    public static void pressEnter() {
        System.out.print("\n  Press ENTER to continue...");
        scanner.nextLine();
    }
}
