package com.perftest.config;

import java.util.Map;

/**
 * Configuration class for test execution parameters.
 */
public class ExecutionConfig {
    private int threads;
    private int iterations;
    private int rampUpSeconds;
    private int holdSeconds;
    private int duration;
    private double successThreshold;
    private Map<String, Object> variables;

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getRampUpSeconds() {
        return rampUpSeconds;
    }

    public void setRampUpSeconds(int rampUpSeconds) {
        this.rampUpSeconds = rampUpSeconds;
    }

    public int getHoldSeconds() {
        return holdSeconds;
    }

    public void setHoldSeconds(int holdSeconds) {
        this.holdSeconds = holdSeconds;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getSuccessThreshold() {
        return successThreshold;
    }

    public void setSuccessThreshold(double successThreshold) {
        this.successThreshold = successThreshold;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "ExecutionConfig{" +
                "threads=" + threads +
                ", iterations=" + iterations +
                ", rampUpSeconds=" + rampUpSeconds +
                ", holdSeconds=" + holdSeconds +
                ", duration=" + duration +
                ", successThreshold=" + successThreshold +
                ", variables=" + variables +
                '}';
    }
}
