package com.perftest.logging;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * TestLogger class for the Performance Automation Framework.
 * Simplified to use log4j2 directly without custom file writing.
 */
public class TestLogger {
    private static final Logger LOGGER = LogManager.getLogger(TestLogger.class);
    private static final String LOG_DIRECTORY = "target/logs";
    private static final String LOG_FILE = "test_run_458.log";

    /**
     * Initializes the logger and ensures the log directory exists.
     */
    public static void initialize() {
        try {
            // Ensure log directory exists
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                if (logDir.mkdirs()) {
                    LOGGER.info("Created log directory: {}", logDir.getAbsolutePath());
                } else {
                    LOGGER.warn("Failed to create log directory: {}", logDir.getAbsolutePath());
                }
            }

            // Set the test run ID in the thread context for logging
            ThreadContext.put("testRunId", "458");

            LOGGER.info("Logger initialized. Logs will be saved to: {}/{}", LOG_DIRECTORY, LOG_FILE);
        } catch (Exception e) {
            LOGGER.error("Error initializing logger: {}", e.getMessage(), e);
        }
    }

    /**
     * Logs the start of a test execution.
     *
     * @param testName The name of the test being executed
     * @param params   Any parameters relevant to the test
     */
    public static void logTestStart(String testName, Object... params) {
        String paramStr = params.length > 0 ? " with params: " + String.join(", ",
                java.util.Arrays.stream(params)
                        .map(Object::toString)
                        .toArray(String[]::new))
                : "";
        LOGGER.info("=== TEST START: {} {} ===", testName, paramStr);
    }

    /**
     * Logs the end of a test execution.
     *
     * @param testName The name of the test that was executed
     * @param success  Whether the test passed or failed
     * @param duration The duration of the test in milliseconds
     */
    public static void logTestEnd(String testName, boolean success, long duration) {
        String result = success ? "PASSED" : "FAILED";
        LOGGER.info("=== TEST END: {} - {} (Duration: {}ms) ===", testName, result, duration);
    }

    /**
     * Logs test steps with proper indentation for better readability.
     *
     * @param stepName The name of the step being executed
     */
    public static void logTestStep(String stepName) {
        LOGGER.info("  STEP: {}", stepName);
    }

    /**
     * Logs test assertions.
     *
     * @param assertion The assertion being checked
     * @param success   Whether the assertion passed or failed
     */
    public static void logAssertion(String assertion, boolean success) {
        if (success) {
            LOGGER.info("    ASSERT PASS: {}", assertion);
        } else {
            LOGGER.error("    ASSERT FAIL: {}", assertion);
        }
    }

    /**
     * Logs API requests.
     *
     * @param protocol The protocol being used (HTTP, SOAP, GraphQL)
     * @param endpoint The endpoint being called
     * @param method   The HTTP method being used
     */
    public static void logApiRequest(String protocol, String endpoint, String method) {
        LOGGER.info("  API REQUEST: {} {} {}", protocol, method, endpoint);
    }

    /**
     * Logs general information messages.
     *
     * @param message The information message to log
     */
    public static void logInfo(String message) {
        LOGGER.info(message);
    }

    /**
     * Logs errors with detailed information.
     *
     * @param message The error message
     * @param error   The error/exception that occurred
     */
    public static void logError(String message, Throwable error) {
        LOGGER.error("ERROR: {} - {}", message, error.getMessage(), error);
    }

    /**
     * Gets the current log file path.
     *
     * @return The path to the current log file
     */
    public static String getLogFilePath() {
        return Paths.get(LOG_DIRECTORY, LOG_FILE).toString();
    }

    /**
     * Archives the current log file with a timestamp.
     */
    public static void archiveLogFile() {
        try {
            Path source = Paths.get(LOG_DIRECTORY, LOG_FILE);
            if (Files.exists(source)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                Path target = Paths.get(LOG_DIRECTORY, "archived_" + timestamp + "_" + LOG_FILE);
                Files.copy(source, target);
                LOGGER.info("Archived log file to: {}", target);
            }
        } catch (Exception e) {
            LOGGER.error("Error archiving log file: {}", e.getMessage(), e);
        }
    }
}