package com.perftest.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.perftest.config.RequestTemplateIntegration;
import com.perftest.core.TemplateProcessor;

/**
 * Service class for managing template-based request generation in tests
 */
public class TemplateRequestService {
    private final String templatesDir;
    private final String dataDir;

    /**
     * Create a new template request service with the given directories
     * 
     * @param templatesDir The directory containing template files
     * @param dataDir      The directory containing CSV data files
     */
    public TemplateRequestService(String templatesDir, String dataDir) {
        this.templatesDir = templatesDir;
        this.dataDir = dataDir;
    }

    /**
     * Get the template path for a given protocol and name
     * 
     * @param protocol The protocol (http, graphql, soap)
     * @param name     The template name
     * @return The full path to the template file
     */
    public String getTemplatePath(String protocol, String name) {
        return Paths.get(templatesDir, protocol, name + ".jinja").toString();
    }

    /**
     * Get the data file path for a given protocol and name
     * 
     * @param protocol The protocol (http, graphql, soap)
     * @param name     The data file name
     * @return The full path to the data file
     */
    public String getDataPath(String protocol, String name) {
        return Paths.get(dataDir, protocol, name + ".csv").toString();
    }

    /**
     * Process a template with a single set of variables
     * 
     * @param templatePath The path to the template file
     * @param variables    The variables to substitute in the template
     * @return The processed template as a string
     */
    public String processTemplate(String templatePath, Map<String, Object> variables) {
        return TemplateProcessor.processTemplateFile(templatePath, variables);
    }

    /**
     * Process a template with data from a CSV file
     * 
     * @param templatePath The path to the template file
     * @param dataPath     The path to the CSV data file
     * @return A list of processed templates as strings
     */
    public List<String> processTemplateWithCsvData(String templatePath, String dataPath) throws IOException {
        return RequestTemplateIntegration.processHttpTemplates(templatePath, dataPath);
    }

    /**
     * Demonstrate template rendering for different protocols
     */
    public static void main(String[] args) {
        TemplateRequestService service = new TemplateRequestService(
                "src/test/resources/body",
                "src/test/resources/data");

        try {
            // Render a template with specific variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("id", 2001);
            variables.put("name", "Alice Williams");
            variables.put("email", "alice@example.com");
            variables.put("timestamp", System.currentTimeMillis());

            // Additional variables for HTTP requests
            variables.put("method", "POST");
            variables.put("base_url", "https://api.example.com");
            variables.put("endpoint", "users");
            variables.put("content_type", "application/json");
            variables.put("auth_type", "Bearer");
            variables.put("auth_token", "my-token-123");

            String result = service.processTemplate(
                    "src/test/resources/body/http/user_profile_template.jinja",
                    variables);

            System.out.println("Rendered template with custom variables:");
            System.out.println(result);

        } catch (Exception e) {
            System.err.println("Error in template service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
