package com.perftest.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for resolving variables in strings.
 */
public class VariableResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(VariableResolver.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * Resolves variables in a string using the provided variables map.
     *
     * @param input The input string containing variables
     * @param variables The variables map for resolution
     * @return The resolved string
     */
    public String resolveVariables(String input, Map<String, Object> variables) {
        if (input == null) {
            return null;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            
            if (value != null) {
                // Replace matched variable with its value
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(value.toString()));
            } else {
                // Keep the original variable reference if not found
                LOGGER.warn("Variable {} not found in context", variableName);
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Adds a runtime variable to the variables map.
     *
     * @param variables The variables map
     * @param name The variable name
     * @param value The variable value
     */
    public void addRuntimeVariable(Map<String, Object> variables, String name, Object value) {
        variables.put(name, value);
    }

    /**
     * Sets the iteration counter as a variable in the variables map.
     *
     * @param variables The variables map
     * @param iteration The current iteration number
     */
    public void setIterationVariable(Map<String, Object> variables, int iteration) {
        variables.put("iteration", iteration);
    }
}
