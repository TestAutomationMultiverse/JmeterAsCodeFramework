package com.perftest.utils;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * Utility class for logging operations and test results.
 */
public class LogUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtils.class);

    /**
     * Logs the summary of test plan execution statistics.
     * This implementation uses regex pattern matching to extract
     * metrics from the string representation of test stats.
     *
     * @param stats The test plan statistics
     */
    public static void logTestPlanStats(TestPlanStats stats) {
        LOGGER.info("==================================================");
        LOGGER.info("TEST EXECUTION SUMMARY");
        LOGGER.info("==================================================");
        LOGGER.info("Raw statistics: {}", stats);

        // Extract metrics using regex for overall stats
        String statsStr = stats.toString();
        long samples = extractLongMetric(statsStr, "samples=(\\d+)");
        long errors = extractLongMetric(statsStr, "errors=(\\d+)");
        double avgResponseTime = extractMetric(statsStr, "avg=(\\d+(\\.\\d+)?)");
        double minResponseTime = extractMetric(statsStr, "min=(\\d+(\\.\\d+)?)");
        double maxResponseTime = extractMetric(statsStr, "max=(\\d+(\\.\\d+)?)");
        double standardDeviation = extractMetric(statsStr, "stdDev=(\\d+(\\.\\d+)?)");
        double throughput = extractMetric(statsStr, "tps=(\\d+(\\.\\d+)?)");

        // Calculate error percentage
        double errorPercentage = samples > 0 ? (double) errors / samples * 100 : 0;

        // Log formatted statistics
        LOGGER.info("Overall Statistics:");
        LOGGER.info("  Total Samples: {}", samples);
        LOGGER.info("  Errors: {} ({}%)", errors, String.format("%.2f", errorPercentage));
        LOGGER.info("  Success Rate: {}%", String.format("%.2f", 100 - errorPercentage));
        LOGGER.info("  Average Response Time: {} ms", String.format("%.2f", avgResponseTime));
        LOGGER.info("  Min Response Time: {} ms", String.format("%.2f", minResponseTime));
        LOGGER.info("  Max Response Time: {} ms", String.format("%.2f", maxResponseTime));
        LOGGER.info("  Standard Deviation: {} ms", String.format("%.2f", standardDeviation));
        LOGGER.info("  Throughput: {} req/sec", String.format("%.2f", throughput));

        // Individual sampler statistics would be processed here if available
        LOGGER.info("==================================================");
    }

    /**
     * Extracts a numeric metric from a statistics string using regex.
     *
     * @param statsStr   The string containing statistics
     * @param patternStr The regex pattern to extract the metric
     * @return The extracted value or 0 if not found
     */
    private static double extractMetric(String statsStr, String patternStr) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
        java.util.regex.Matcher matcher = pattern.matcher(statsStr);

        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to parse metric with pattern {}: {}", patternStr, e.getMessage());
            }
        }

        return 0; // Default if not found or parsing fails
    }

    /**
     * Extracts a long metric from a statistics string using regex.
     *
     * @param statsStr   The string containing statistics
     * @param patternStr The regex pattern to extract the metric
     * @return The extracted value as long or 0 if not found
     */
    private static long extractLongMetric(String statsStr, String patternStr) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
        java.util.regex.Matcher matcher = pattern.matcher(statsStr);

        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to parse long metric with pattern {}: {}", patternStr, e.getMessage());
            }
        }

        return 0; // Default if not found or parsing fails
    }

    /**
     * Formats a duration in a human-readable format.
     *
     * @param duration The duration to format
     * @return A human-readable string representation of the duration
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);

        if (absSeconds < 60) {
            return seconds + " seconds";
        }

        long minutes = absSeconds / 60;
        long remainingSeconds = absSeconds % 60;

        if (minutes < 60) {
            return String.format("%d minutes, %d seconds", minutes, remainingSeconds);
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        return String.format("%d hours, %d minutes, %d seconds", hours, remainingMinutes, remainingSeconds);
    }
}
