package com.medlab.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight performance metrics tracker.
 *
 * Tracks wall-clock duration of key operations and prints a summary.
 * Target: < 300ms for most DB operations (per HLRD NFR).
 *
 * Future (Spring Boot v2): Replaced by Micrometer + Prometheus/Grafana.
 */
public class PerformanceMetrics {

    private static final long WARN_THRESHOLD_MS = 300L;

    // Stores operation name → last recorded duration in ms
    private static final Map<String, Long> metrics = new LinkedHashMap<>();

    private PerformanceMetrics() {}

    /**
     * Records how long an operation took (in ms).
     * Prints a warning if it exceeds the threshold.
     */
    public static void record(String operation, long durationMs) {
        metrics.put(operation, durationMs);
        if (durationMs > WARN_THRESHOLD_MS) {
            System.out.printf("  ⚠  [PERF] '%s' took %d ms (> %d ms threshold)%n",
                operation, durationMs, WARN_THRESHOLD_MS);
        }
    }

    /**
     * Convenience: runs a block and records its duration.
     */
    public static <T> T measure(String operation, ThrowingSupplier<T> action) throws Exception {
        long start  = System.currentTimeMillis();
        T    result = action.get();
        long end    = System.currentTimeMillis();
        record(operation, end - start);
        return result;
    }

    /** Prints a summary of all recorded metrics to stdout. */
    public static void printSummary() {
        if (metrics.isEmpty()) return;
        System.out.println("\n  ── Performance Summary ──────────────────────");
        metrics.forEach((op, ms) ->
            System.out.printf("  %-40s %4d ms%s%n",
                op, ms, ms > WARN_THRESHOLD_MS ? "  ⚠" : "  ✔"));
        System.out.println("  ─────────────────────────────────────────────");
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
