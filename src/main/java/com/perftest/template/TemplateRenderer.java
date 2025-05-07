package com.perftest.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * TemplateRenderer - A utility class for rendering templates
 * intended for integration with the Performance Automation Framework.
 * This implementation uses a simple template engine with Jinjava-like syntax.
 */
public class TemplateRenderer {
    private JinjavaTemplateProcessor processor;

    // // Patterns for variable replacement
    // private static final Pattern JINJA_VARIABLE_PATTERN =
    // Pattern.compile("\\{\\{\\s*([\\w\\.]+)\\s*\\}\\}");
    // private static final Pattern PROPERTY_VARIABLE_PATTERN =
    // Pattern.compile("\\$\\{([\\w\\.]+)\\}");

    /**
     * Constructor
     */
    public TemplateRenderer() {
        this.processor = new JinjavaTemplateProcessor();
    }

    /**
     * Render a template file with the given context data
     * 
     * @param templatePath The path to the template file
     * @param contextData  The context data for variable substitution
     * @return The rendered template
     */
    public String renderTemplateFile(String templatePath, Map<String, String> contextData) throws IOException {
        String templateContent = Files.readString(Path.of(templatePath));
        return renderTemplate(templateContent, contextData);
    }

    /**
     * Render a template string with the given context data
     * using a template engine that supports Jinjava-style syntax
     * 
     * @param templateContent The template content as a string
     * @param contextData     The context data for variable substitution
     * @return The rendered template
     */
    public String renderTemplate(String templateContent, Map<String, String> contextData) {
        // Convert String map to Object map for template processor
        Map<String, Object> objectContextData = convertToObjectMap(contextData);

        // Use our template processor to render the template
        return processor.renderTemplate(templateContent, objectContextData);
    }

    /**
     * Convert a map with string values to a map with object values,
     * handling nested properties with dot notation
     * 
     * @param stringMap The map with string values
     * @return A map with object values
     */
    private Map<String, Object> convertToObjectMap(Map<String, String> stringMap) {
        Map<String, Object> objectMap = new HashMap<>();

        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            // Handle dot notation for nested properties
            if (entry.getKey().contains(".")) {
                processNestedProperty(objectMap, entry.getKey(), entry.getValue());
            } else {
                objectMap.put(entry.getKey(), entry.getValue());
            }
        }

        return objectMap;
    }

    /**
     * Process a nested property with dot notation
     * For example, "pagination.limit" becomes {"pagination": {"limit": value}}
     * 
     * @param contextData The context data map to add the property to
     * @param key         The key with dot notation
     * @param value       The value to set
     */
    @SuppressWarnings("unchecked")
    private void processNestedProperty(Map<String, Object> contextData, String key, String value) {
        String[] parts = key.split("\\.");
        String rootKey = parts[0];

        // Ensure the root map exists
        if (!contextData.containsKey(rootKey)) {
            contextData.put(rootKey, new HashMap<String, Object>());
        }

        // Get the nested map
        Map<String, Object> nestedMap = (Map<String, Object>) contextData.get(rootKey);

        // Set the value in the nested map
        nestedMap.put(parts[1], value);
    }
}