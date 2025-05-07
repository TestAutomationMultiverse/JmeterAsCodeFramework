package com.perftest.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for managing variables at different levels (global, scenario, request).
 */
public class VariablesConfig {
    private Map<String, Object> globalVariables = new HashMap<>();
    private Map<String, Object> executionVariables = new HashMap<>();
    private Map<String, Object> scenarioVariables = new HashMap<>();
    private Map<String, Object> requestVariables = new HashMap<>();

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public Map<String, Object> getExecutionVariables() {
        return executionVariables;
    }

    public void setExecutionVariables(Map<String, Object> executionVariables) {
        this.executionVariables = executionVariables;
    }

    public Map<String, Object> getScenarioVariables() {
        return scenarioVariables;
    }

    public void setScenarioVariables(Map<String, Object> scenarioVariables) {
        this.scenarioVariables = scenarioVariables;
    }

    public Map<String, Object> getRequestVariables() {
        return requestVariables;
    }

    public void setRequestVariables(Map<String, Object> requestVariables) {
        this.requestVariables = requestVariables;
    }

    /**
     * Merges all variables with proper precedence (request > scenario > execution > global)
     * @return A merged map of all variables
     */
    public Map<String, Object> getMergedVariables() {
        Map<String, Object> merged = new HashMap<>(globalVariables);
        merged.putAll(executionVariables);
        merged.putAll(scenarioVariables);
        merged.putAll(requestVariables);
        return merged;
    }

    /**
     * Gets a variable value by name, respecting precedence
     * @param name The variable name
     * @return The variable value or null if not found
     */
    public Object getVariable(String name) {
        if (requestVariables.containsKey(name)) {
            return requestVariables.get(name);
        } else if (scenarioVariables.containsKey(name)) {
            return scenarioVariables.get(name);
        } else if (executionVariables.containsKey(name)) {
            return executionVariables.get(name);
        } else {
            return globalVariables.get(name);
        }
    }

    @Override
    public String toString() {
        return "VariablesConfig{" +
                "globalVariables=" + globalVariables +
                ", executionVariables=" + executionVariables +
                ", scenarioVariables=" + scenarioVariables +
                ", requestVariables=" + requestVariables +
                '}';
    }
}
