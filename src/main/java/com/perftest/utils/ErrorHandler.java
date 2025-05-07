package com.perftest.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Enhanced error handling utility for the Performance Automation Framework.
 * Provides methods to safely execute code with proper error handling and logging.
 */
public class ErrorHandler {
    private static final Logger LOGGER = LogManager.getLogger(ErrorHandler.class);
    
    /**
     * Executes the provided operation safely, logging any exceptions that occur.
     *
     * @param operation The operation to execute
     * @param errorMessage The error message to log if an exception occurs
     * @param <T> The return type of the operation
     * @return The result of the operation, or null if an exception occurred
     */
    public static <T> T executeSafely(Callable<T> operation, String errorMessage) {
        try {
            return operation.call();
        } catch (Exception e) {
            LOGGER.error("{}. Error: {}", errorMessage, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Executes the provided operation safely with a default value to return on error.
     *
     * @param operation The operation to execute
     * @param errorMessage The error message to log if an exception occurs
     * @param defaultValue The default value to return if an exception occurs
     * @param <T> The return type of the operation
     * @return The result of the operation, or the default value if an exception occurred
     */
    public static <T> T executeSafely(Callable<T> operation, String errorMessage, T defaultValue) {
        try {
            return operation.call();
        } catch (Exception e) {
            LOGGER.error("{}. Error: {}", errorMessage, e.getMessage(), e);
            return defaultValue;
        }
    }
    
    /**
     * Executes the provided void operation safely, logging any exceptions that occur.
     *
     * @param operation The operation to execute
     * @param errorMessage The error message to log if an exception occurs
     */
    public static void executeSafely(Runnable operation, String errorMessage) {
        try {
            operation.run();
        } catch (Exception e) {
            LOGGER.error("{}. Error: {}", errorMessage, e.getMessage(), e);
        }
    }
    
    /**
     * Handles a specific exception with the provided handler.
     *
     * @param operation The operation to execute
     * @param exceptionClass The exception class to handle
     * @param handler The handler to invoke with the exception
     * @param <T> The return type of the operation
     * @param <E> The type of exception to handle
     * @return The result of the operation, or null if the expected exception occurred
     */
    public static <T, E extends Exception> T handleSpecificException(
            Callable<T> operation, 
            Class<E> exceptionClass, 
            Consumer<E> handler) {
        try {
            return operation.call();
        } catch (Exception e) {
            if (exceptionClass.isInstance(e)) {
                handler.accept(exceptionClass.cast(e));
                return null;
            }
            LOGGER.error("Unexpected exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Retries an operation with exponential backoff.
     *
     * @param operation The operation to retry
     * @param maxRetries The maximum number of retries
     * @param initialDelayMs The initial delay in milliseconds
     * @param operationName The name of the operation for logging purposes
     * @param <T> The return type of the operation
     * @return The result of the operation, or null if all retries failed
     */
    public static <T> T retryWithBackoff(
            Callable<T> operation, 
            int maxRetries, 
            long initialDelayMs,
            String operationName) {
        int retries = 0;
        long delay = initialDelayMs;
        
        while (retries <= maxRetries) {
            try {
                if (retries > 0) {
                    LOGGER.info("Retry attempt {} for operation: {}", retries, operationName);
                }
                return operation.call();
            } catch (Exception e) {
                retries++;
                if (retries > maxRetries) {
                    LOGGER.error("Failed to execute {} after {} retries. Error: {}", 
                                operationName, maxRetries, e.getMessage(), e);
                    return null;
                }
                
                LOGGER.warn("Error during {} (attempt {}/{}). Retrying in {} ms. Error: {}", 
                           operationName, retries, maxRetries, delay, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("Retry interrupted: {}", ie.getMessage());
                    return null;
                }
                
                // Exponential backoff with a maximum of 30 seconds
                delay = Math.min(delay * 2, 30000);
            }
        }
        
        return null;
    }
}