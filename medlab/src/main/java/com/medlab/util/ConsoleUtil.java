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

    /**
     * Reads a string but masks the input with asterisks (simulated).
     * Full masking requires JNI/Console; this uses Console if available,
     * otherwise falls back to plain read.
     */
    public static String readPassword(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            return new String(pwd);
        }
        // Fallback for IDE terminals
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

    public static void printWarning(String msg) {
        System.out.println("  ⚠  WARNING: " + msg);
    }

    public static void pressEnter() {
        System.out.print("\n  Press ENTER to continue...");
        scanner.nextLine();
    }
}
