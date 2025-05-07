package com.perftest.core;

import com.perftest.template.TemplateRenderer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;

/**
 * TemplateProcessor - Integration class that connects the TemplateRenderer
 * with the rest of the Performance Automation Framework
 */
public class TemplateProcessor {
    private static TemplateRenderer renderer = new TemplateRenderer();
    
    /**
     * Process a template file with the given context data
     * 
     * @param templatePath The path to the template file
     * @param contextData The context data for variable substitution
     * @return The rendered template
     */
    public static String processTemplateFile(String templatePath, Map<String, Object> contextData) {
        try {
            if (renderer == null) {
                renderer = new TemplateRenderer();
            }
            
            // Convert Object map to String map (simplified implementation)
            Map<String, String> stringContextData = new HashMap<>();
            for (Map.Entry<String, Object> entry : contextData.entrySet()) {
                stringContextData.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            
            return renderer.renderTemplateFile(templatePath, stringContextData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process a template string with the given context data
     * 
     * @param templateContent The template content as a string
     * @param contextData The context data for variable substitution
     * @return The rendered template
     */
    public static String processTemplateString(String templateContent, Map<String, Object> contextData) {
        try {
            if (renderer == null) {
                renderer = new TemplateRenderer();
            }
            
            // Convert Object map to String map (simplified implementation)
            Map<String, String> stringContextData = new HashMap<>();
            for (Map.Entry<String, Object> entry : contextData.entrySet()) {
                stringContextData.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            
            return renderer.renderTemplate(templateContent, stringContextData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load an external query file content
     * This is used when configuration references an external file rather than inline content
     * 
     * @param filePath The path to the external query/envelope file
     * @param basePath Optional base path to prepend (default: src/test/resources/)
     * @return The file content as a String
     */
    public static String loadExternalFile(String filePath, String basePath) {
        try {
            // Default base path if not specified
            if (basePath == null || basePath.isEmpty()) {
                basePath = "src/test/resources/";
            }
            
            // Make sure path doesn't have double slashes
            if (basePath.endsWith("/") && filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            } else if (!basePath.endsWith("/") && !filePath.startsWith("/")) {
                basePath = basePath + "/";
            }
            
            // Load file content
            Path path = Paths.get(basePath + filePath);
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load external file: " + filePath + ". Error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load external file with default base path
     * 
     * @param filePath The path to the external query/envelope file
     * @return The file content as a String
     */
    public static String loadExternalFile(String filePath) {
        return loadExternalFile(filePath, null);
    }
}