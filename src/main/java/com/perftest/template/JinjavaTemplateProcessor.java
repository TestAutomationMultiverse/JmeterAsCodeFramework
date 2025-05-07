package com.perftest.template;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JinjavaTemplateProcessor - A utility class for processing templates with
 * Jinjava-like syntax
 * with CSV data for dynamic request generation in performance testing
 * frameworks.
 * This is a simplified implementation that doesn't require the actual Jinjava
 * library.
 */
public class JinjavaTemplateProcessor {

    // Patterns for variable replacement
    private static final Pattern JINJA_VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([\\w\\.]+)\\s*\\}\\}");
    private static final Pattern PROPERTY_VARIABLE_PATTERN = Pattern.compile("\\$\\{([\\w\\.]+)\\}");

    /**
     * Main method for demonstrating the template processor functionality
     */
    public static void main(String[] args) {
        JinjavaTemplateProcessor processor = new JinjavaTemplateProcessor();

        try {
            // Example paths for templates and CSV data
            String templatePath = "src/test/resources/body/http/user_profile_template.jinja";
            String csvPath = "src/test/resources/data/http/test_data.csv";

            // Process the template with CSV data
            List<String> results = processor.processTemplateWithCsvData(templatePath, csvPath);

            // Print the results
            System.out.println("\nTemplate processing results:");
            for (int i = 0; i < results.size(); i++) {
                System.out.println("\nRendered template for row " + (i + 1) + ":");
                System.out.println(results.get(i));
            }

            System.out.println("\n-------------------------------------------------------------------------");
            System.out.println("NOTE: This is a simplified implementation to demonstrate the concept.");
            System.out.println("In a production environment, this would use the actual Jinjava library");
            System.out.println("and would be integrated with the Performance Automation Framework.");
            System.out.println("-------------------------------------------------------------------------");
        } catch (Exception e) {
            System.err.println("Error processing template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process a template with data from a CSV file
     * 
     * @param templatePath The path to the template file
     * @param csvPath      The path to the CSV data file
     * @return A list of rendered templates, one for each row in the CSV
     */
    public List<String> processTemplateWithCsvData(String templatePath, String csvPath) throws IOException {
        // Read the template content
        String templateContent = readFile(templatePath);

        // Parse the CSV data
        List<Map<String, Object>> dataRows = parseCSV(csvPath);

        // Process each row of data
        List<String> results = new ArrayList<>();
        for (Map<String, Object> row : dataRows) {
            // Add timestamp if not already present
            if (!row.containsKey("timestamp")) {
                row.put("timestamp", String.valueOf(System.currentTimeMillis()));
            }
            results.add(renderTemplate(templateContent, row));
        }
        return results;
    }

    /**
     * Render a template with the given context data
     * using a simple template engine that supports Jinjava-style syntax
     * This implementation handles variable names with case-insensitive matching
     * 
     * @param templateContent The template content
     * @param contextData     The context data for variable substitution
     * @return The rendered template
     */
    public String renderTemplate(String templateContent, Map<String, Object> contextData) {
        // Flatten nested data for easier lookup with case-insensitive matching
        Map<String, String> flattenedContextData = flattenContextData(contextData);

        // Replace Jinjava-style variables: {{ variable }} or {{variable}}
        StringBuffer result = new StringBuffer();
        Matcher jinjaVarMatcher = JINJA_VARIABLE_PATTERN.matcher(templateContent);

        while (jinjaVarMatcher.find()) {
            String variableName = jinjaVarMatcher.group(1);
            String replacement = flattenedContextData.getOrDefault(variableName, jinjaVarMatcher.group());

            // If variable not found, try similar variable names
            if (replacement.equals(jinjaVarMatcher.group()) && variableName.equalsIgnoreCase("city")) {
                replacement = flattenedContextData.getOrDefault("cityName", jinjaVarMatcher.group());
            } else if (replacement.equals(jinjaVarMatcher.group()) && variableName.equalsIgnoreCase("cityName")) {
                replacement = flattenedContextData.getOrDefault("city", jinjaVarMatcher.group());
            }

            // Need to escape dollar signs and backslashes in the replacement string
            replacement = replacement.replace("$", "\\$").replace("\\", "\\\\");
            jinjaVarMatcher.appendReplacement(result, replacement);
        }
        jinjaVarMatcher.appendTail(result);

        String afterJinjaReplacement = result.toString();

        // Replace property-style variables: ${variable}
        result = new StringBuffer();
        Matcher propVarMatcher = PROPERTY_VARIABLE_PATTERN.matcher(afterJinjaReplacement);

        while (propVarMatcher.find()) {
            String variableName = propVarMatcher.group(1);
            String replacement = flattenedContextData.getOrDefault(variableName, propVarMatcher.group());

            // If variable not found, try similar variable names
            if (replacement.equals(propVarMatcher.group()) && variableName.equalsIgnoreCase("city")) {
                replacement = flattenedContextData.getOrDefault("cityName", propVarMatcher.group());
            } else if (replacement.equals(propVarMatcher.group()) && variableName.equalsIgnoreCase("cityName")) {
                replacement = flattenedContextData.getOrDefault("city", propVarMatcher.group());
            }

            // Need to escape dollar signs and backslashes in the replacement string
            replacement = replacement.replace("$", "\\$").replace("\\", "\\\\");
            propVarMatcher.appendReplacement(result, replacement);
        }
        propVarMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Flatten nested context data with dot notation
     * For example, {"user": {"name": "John"}} becomes {"user.name": "John"}
     * This implementation adds case-insensitive matching for variable names
     * 
     * @param contextData The original context data
     * @return Flattened map with dot notation keys
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> flattenContextData(Map<String, Object> contextData) {
        Map<String, String> flattened = new HashMap<>();
        Map<String, String> caseInsensitiveMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : contextData.entrySet()) {
            if (entry.getValue() instanceof Map) {
                // Handle nested maps (e.g., pagination.limit)
                Map<String, Object> nestedMap = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                    String nestedKey = entry.getKey() + "." + nestedEntry.getKey();
                    String valueStr = String.valueOf(nestedEntry.getValue());
                    flattened.put(nestedKey, valueStr);
                    caseInsensitiveMap.put(nestedKey.toLowerCase(), valueStr);
                }
            }
            // Add the original entry as well
            String valueStr = String.valueOf(entry.getValue());
            flattened.put(entry.getKey(), valueStr);
            caseInsensitiveMap.put(entry.getKey().toLowerCase(), valueStr);

            // Add specific case-insensitive mappings for common naming conventions
            // This helps with common naming convention mismatches like "city" vs "cityName"
            if ("city".equalsIgnoreCase(entry.getKey())) {
                flattened.put("cityName", valueStr);
                caseInsensitiveMap.put("cityname", valueStr);
            } else if ("cityName".equalsIgnoreCase(entry.getKey())) {
                flattened.put("city", valueStr);
                caseInsensitiveMap.put("city", valueStr);
            }
        }

        return new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getOrDefault(Object key, String defaultValue) {
                // First try exact match
                if (flattened.containsKey(key)) {
                    return flattened.get(key);
                }

                // Then try case-insensitive match
                if (key instanceof String && caseInsensitiveMap.containsKey(((String) key).toLowerCase())) {
                    return caseInsensitiveMap.get(((String) key).toLowerCase());
                }

                return defaultValue;
            }

            @Override
            public String get(Object key) {
                return getOrDefault(key, null);
            }

            @Override
            public boolean containsKey(Object key) {
                return flattened.containsKey(key) ||
                        (key instanceof String && caseInsensitiveMap.containsKey(((String) key).toLowerCase()));
            }

            // Forward other method calls to the flattened map
            @Override
            public int size() {
                return flattened.size();
            }

            @Override
            public boolean isEmpty() {
                return flattened.isEmpty();
            }
        };
    }

    /**
     * Parse a CSV file into a list of maps, where each map represents a row
     * with column names as keys
     * This implementation handles nested properties with dot notation
     * 
     * @param csvPath The path to the CSV file
     * @return A list of maps, each representing a row of data
     */
    private List<Map<String, Object>> parseCSV(String csvPath) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            // Read the header row to get column names
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");

            // Process each data row
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, Object> row = new HashMap<>();

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String header = headers[i].trim();
                    String value = values[i].trim();

                    // Handle nested properties with dot notation
                    if (header.contains(".")) {
                        processNestedProperty(row, header, value);
                    } else {
                        row.put(header, value);
                    }
                }

                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Process a nested property with dot notation
     * For example, "pagination.limit" becomes {"pagination": {"limit": value}}
     * 
     * @param row    The row map to add the property to
     * @param header The header with dot notation
     * @param value  The value to set
     */
    @SuppressWarnings("unchecked")
    private void processNestedProperty(Map<String, Object> row, String header, String value) {
        String[] parts = header.split("\\.");
        String rootKey = parts[0];

        // Ensure the root map exists
        if (!row.containsKey(rootKey)) {
            row.put(rootKey, new HashMap<String, Object>());
        }

        // Get the nested map
        Map<String, Object> nestedMap = (Map<String, Object>) row.get(rootKey);

        // Set the value in the nested map
        nestedMap.put(parts[1], value);
    }

    /**
     * Read a file's content as a string
     * 
     * @param filePath The path to the file
     * @return The file content as a string
     */
    private String readFile(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }
}