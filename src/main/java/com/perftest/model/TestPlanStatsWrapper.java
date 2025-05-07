package com.perftest.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * A wrapper around TestPlanStats to provide additional functionality
 * specifically for error handling during report generation.
 * This class acts as a decorator for TestPlanStats and adds error tracking functionality.
 */
public class TestPlanStatsWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPlanStatsWrapper.class);
    
    private TestPlanStats stats;
    private boolean errorDuringTest = false;
    private String errorMessage = null;
    
    /**
     * Creates a new TestPlanStatsWrapper with the given TestPlanStats.
     * 
     * @param stats The TestPlanStats to wrap
     */
    public TestPlanStatsWrapper(TestPlanStats stats) {
        this.stats = stats;
    }
    
    /**
     * Creates a new TestPlanStatsWrapper for error situations where
     * no valid TestPlanStats is available.
     * 
     * @param errorMessage The error message describing what went wrong
     */
    public TestPlanStatsWrapper(String errorMessage) {
        this.stats = null;
        this.errorDuringTest = true;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Gets the wrapped TestPlanStats.
     * 
     * @return The wrapped TestPlanStats, or null if there was an error
     */
    public TestPlanStats getStats() {
        return stats;
    }
    
    /**
     * Checks if there was an error during the test execution.
     * 
     * @return True if there was an error, false otherwise
     */
    public boolean hasError() {
        return errorDuringTest;
    }
    
    /**
     * Sets the error flag for this test execution.
     * 
     * @param errorDuringTest The error flag
     */
    public void setErrorDuringTest(boolean errorDuringTest) {
        this.errorDuringTest = errorDuringTest;
    }
    
    /**
     * Gets the error message if there was an error.
     * 
     * @return The error message, or null if there was no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Sets the error message for this test execution.
     * 
     * @param errorMessage The error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorMessage != null) {
            this.errorDuringTest = true;
        }
    }
    
    /**
     * Gets the overall success rate from the wrapped TestPlanStats.
     * 
     * @return The overall success rate, or 0 if there was an error
     */
    public double getOverallSuccessRate() {
        if (stats == null) {
            return 0;
        }
        
        try {
            // Calculate success rate based on total samples and errors
            long totalSamples = stats.overall().samplesCount();
            long errorSamples = stats.overall().errorsCount();
            
            if (totalSamples == 0) {
                return 0;
            }
            
            return 100.0 * (totalSamples - errorSamples) / totalSamples;
        } catch (Exception e) {
            LOGGER.error("Error calculating success rate: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Gets the overall error count from the wrapped TestPlanStats.
     * 
     * @return The overall error count, or -1 if there was an error
     */
    public long getOverallErrorCount() {
        if (stats == null) {
            return -1;
        }
        return stats.overall().errorsCount();
    }
    
    /**
     * Checks if the test execution was successful based on error count.
     * 
     * @return True if the test execution was successful, false otherwise
     */
    public boolean isSuccessful() {
        if (errorDuringTest) {
            return false;
        }
        if (stats == null) {
            return false;
        }
        return stats.overall().errorsCount() == 0;
    }
}