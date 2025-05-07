package com.perftest.execution;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.perftest.core.TestExecutor;
import com.perftest.logging.TestLogger;
import com.perftest.utils.ErrorHandler;

/**
 * TestExecutionManager handles the execution of performance tests,
 * integrating logging and error handling.
 */
public class TestExecutionManager {
    private static final Logger LOGGER = LogManager.getLogger(TestExecutionManager.class);
    private final TestExecutor testExecutor;
    private ExecutorService executorService;
    private boolean initialized = false;

    /**
     * Creates a new TestExecutionManager.
     */
    public TestExecutionManager() {
        testExecutor = new TestExecutor();

        // Use a cached thread pool for flexibility
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * Initializes the test execution environment.
     */
    public void initialize() {
        ErrorHandler.executeSafely(() -> {
            // Initialize logger
            TestLogger.initialize();
            LOGGER.info("Initializing test execution environment");

            // Create target directory if it doesn't exist
            File targetDir = new File("target");
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                LOGGER.warn("Failed to create target directory");
            }

            initialized = true;
            LOGGER.info("Test execution environment initialized");
            return null;
        }, "Failed to initialize test execution environment");
    }

    /**
     * Executes a test with the specified configuration file.
     *
     * @param testConfigFile The path to the test configuration file
     * @return True if the test execution was successful, false otherwise
     */
    public boolean executeTest(String testConfigFile) {
        if (!initialized) {
            LOGGER.error("Test execution environment not initialized");
            return false;
        }

        LOGGER.info("Starting test execution for config: {}", testConfigFile);
        TestLogger.logTestStart("PerformanceTest", testConfigFile);

        LocalDateTime startTime = LocalDateTime.now();
        boolean success = false;

        try {
            // Execute the test
            success = testExecutor.execute(testConfigFile);

            // Record the test result
            LocalDateTime endTime = LocalDateTime.now();

            TestLogger.logTestEnd("PerformanceTest", success,
                    java.time.Duration.between(startTime, endTime).toMillis());

            return success;
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            LOGGER.error("Error executing test: {}", e.getMessage(), e);
            TestLogger.logError("Test execution failed", e);
            TestLogger.logTestEnd("PerformanceTest", false,
                    java.time.Duration.between(startTime, endTime).toMillis());

            return false;
        }
    }

    /**
     * Executes multiple tests in parallel.
     *
     * @param testConfigFiles Array of test configuration file paths
     * @return True if all tests passed, false if any failed
     */
    public boolean executeTestsInParallel(String[] testConfigFiles) {
        if (!initialized) {
            LOGGER.error("Test execution environment not initialized");
            return false;
        }

        LOGGER.info("Starting parallel test execution for {} tests", testConfigFiles.length);
        LocalDateTime startTime = LocalDateTime.now();

        CompletableFuture<Boolean>[] futures = new CompletableFuture[testConfigFiles.length];

        for (int i = 0; i < testConfigFiles.length; i++) {
            final String testConfig = testConfigFiles[i];
            final int testIndex = i;

            futures[i] = CompletableFuture.supplyAsync(() -> {
                String testName = "PerformanceTest-" + extractTestName(testConfig) + "-" + testIndex;
                TestLogger.logTestStart(testName, testConfig);
                LocalDateTime testStartTime = LocalDateTime.now();

                try {
                    boolean success = testExecutor.execute(testConfig);

                    LocalDateTime testEndTime = LocalDateTime.now();
                    TestLogger.logTestEnd(testName, success,
                            java.time.Duration.between(testStartTime, testEndTime).toMillis());

                    return success;
                } catch (Exception e) {
                    LocalDateTime testEndTime = LocalDateTime.now();
                    LOGGER.error("Error executing test {}: {}", testName, e.getMessage(), e);
                    TestLogger.logError("Test execution failed for " + testName, e);
                    TestLogger.logTestEnd(testName, false,
                            java.time.Duration.between(testStartTime, testEndTime).toMillis());

                    return false;
                }
            }, executorService);
        }

        // Wait for all tests to complete
        CompletableFuture<Void> allTests = CompletableFuture.allOf(futures);

        try {
            // Wait for all tests to complete
            allTests.get(30, TimeUnit.MINUTES);

            // Check results
            boolean allPassed = true;
            for (CompletableFuture<Boolean> future : futures) {
                allPassed &= future.get();
            }

            LocalDateTime endTime = LocalDateTime.now();
            LOGGER.info("All tests completed. Overall result: {}", allPassed ? "PASSED" : "FAILED");

            return allPassed;
        } catch (Exception e) {
            LOGGER.error("Error waiting for test completion: {}", e.getMessage(), e);
            TestLogger.logError("Parallel test execution failed", e);

            return false;
        }
    }

    /**
     * Extracts a test name from a configuration file path.
     *
     * @param configFile The configuration file path
     * @return A test name based on the configuration file
     */
    private String extractTestName(String configFile) {
        if (configFile == null) {
            return "Unknown";
        }

        // Extract file name without extension
        File file = new File(configFile);
        String filename = file.getName();

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }

        return filename;
    }

    /**
     * Shuts down the test execution environment.
     */
    public void shutdown() {
        ErrorHandler.executeSafely(() -> {
            LOGGER.info("Shutting down test execution environment");

            // Shutdown the executor service
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // Archive the log file
            TestLogger.archiveLogFile();

            LOGGER.info("Test execution environment shutdown complete");
            return null;
        }, "Error shutting down test execution environment");
    }
}