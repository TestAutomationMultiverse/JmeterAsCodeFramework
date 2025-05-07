package com.perftest.config;

import com.perftest.core.TemplateProcessor;
import com.perftest.template.JinjavaTemplateProcessor;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration class for template processing in the Performance Automation Framework
 * This class demonstrates how to use the template processor with different protocol types
 */
public class RequestTemplateIntegration {

    /**
     * Process an HTTP request template with data from the given CSV file
     * 
     * @param templatePath The path to the HTTP template file
     * @param csvPath The path to the CSV data file
     * @return A list of processed request bodies as strings
     */
    public static List<String> processHttpTemplates(String templatePath, String csvPath) throws IOException {
        List<Map<String, Object>> dataRows = parseCSV(csvPath);
        List<String> processedRequests = new ArrayList<>();
        
        for (Map<String, Object> row : dataRows) {
            String processed = TemplateProcessor.processTemplateFile(templatePath, row);
            processedRequests.add(processed);
        }
        
        return processedRequests;
    }
    
    /**
     * Process a GraphQL query template with data from the given CSV file
     * 
     * @param templatePath The path to the GraphQL template file
     * @param csvPath The path to the CSV data file
     * @return A list of processed GraphQL queries as strings
     */
    public static List<String> processGraphQLTemplates(String templatePath, String csvPath) throws IOException {
        return processHttpTemplates(templatePath, csvPath); // Same processing logic
    }
    
    /**
     * Process a SOAP envelope template with data from the given CSV file
     * 
     * @param templatePath The path to the SOAP template file
     * @param csvPath The path to the CSV data file
     * @return A list of processed SOAP envelopes as strings
     */
    public static List<String> processSoapTemplates(String templatePath, String csvPath) throws IOException {
        List<Map<String, Object>> dataRows = parseCSV(csvPath);
        List<String> processedEnvelopes = new ArrayList<>();
        
        for (Map<String, Object> row : dataRows) {
            // Handle the special case of parameters object in SOAP templates
            if (row.containsKey("parameters")) {
                String paramsStr = (String) row.get("parameters");
                // In a real implementation, we would parse the JSON string into a Map
                // For this demonstration, we'll leave it as a string
            }
            
            String processed = TemplateProcessor.processTemplateFile(templatePath, row);
            processedEnvelopes.add(processed);
        }
        
        return processedEnvelopes;
    }
    
    /**
     * Parse a CSV file into a list of maps with variable names as keys
     * 
     * @param csvPath The path to the CSV file
     * @return A list of maps with variable bindings
     */
    private static List<Map<String, Object>> parseCSV(String csvPath) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            String[] headers = headerLine.split(",");
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, Object> row = new HashMap<>();
                
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                
                rows.add(row);
            }
        }
        
        return rows;
    }

    /**
     * Demonstrates loading an external query or envelope file referenced in a configuration
     * 
     * @param filePath The path specified in the configuration
     * @return The content of the referenced file
     */
    public static String loadExternalFileReference(String filePath) throws IOException {
        // Use the TemplateProcessor utility method to load the file content
        return TemplateProcessor.loadExternalFile(filePath);
    }
    
    /**
     * Process a template or static file based configuration value
     * This can handle both inline content and file references
     * 
     * @param configValue The value from the configuration (either inline content or a file path)
     * @param contextData The context data for variable substitution if needed
     * @return The processed content
     */
    public static String processConfigValue(String configValue, Map<String, Object> contextData) throws IOException {
        // Check if this is likely a file reference (doesn't contain typical content markers)
        boolean isFileReference = !configValue.contains("{") && 
                                !configValue.contains("<") && 
                                !configValue.contains(">") &&
                                !configValue.contains("\n");
        
        if (isFileReference) {
            // Load the external file
            String fileContent = loadExternalFileReference(configValue);
            
            // If context data is provided, process it as a template
            if (contextData != null && !contextData.isEmpty()) {
                return TemplateProcessor.processTemplateString(fileContent, contextData);
            } else {
                return fileContent;
            }
        } else {
            // Process inline content as a template if context data is provided
            if (contextData != null && !contextData.isEmpty()) {
                return TemplateProcessor.processTemplateString(configValue, contextData);
            } else {
                return configValue;
            }
        }
    }
    
    /**
     * Demonstrate how to integrate template processing with the existing request classes
     */
    public static void main(String[] args) {
        try {
            // Use JinjavaTemplateProcessor directly to demonstrate its capabilities
            JinjavaTemplateProcessor jinjavaProcessor = new JinjavaTemplateProcessor();
            List<String> httpResults = jinjavaProcessor.processTemplateWithCsvData(
                "src/test/resources/body/http/user_profile_template.jinja",
                "src/test/resources/data/http/test_data.csv"
            );
            
            // Also demonstrate the integration with TemplateProcessor
            System.out.println("Processing HTTP templates:");
            List<String> httpBodies = processHttpTemplates(
                "src/test/resources/body/http/user_profile_template.jinja",
                "src/test/resources/data/http/test_data.csv"
            );
            
            // Show how the templates would be used in an actual HTTP request
            for (int i = 0; i < httpBodies.size(); i++) {
                // Get the data row to access the HTTP method, URL, etc.
                Map<String, Object> requestData = parseCSV("src/test/resources/data/http/test_data.csv").get(i);
                String method = (String) requestData.get("method");
                String baseUrl = (String) requestData.get("base_url");
                String endpoint = (String) requestData.get("endpoint");
                String contentType = (String) requestData.get("content_type");
                String authType = (String) requestData.get("auth_type");
                String authToken = (String) requestData.get("auth_token");
                
                // Create the full request
                System.out.println("\nHTTP Request " + (i+1) + ":");
                System.out.println("Method: " + method);
                System.out.println("URL: " + baseUrl + "/" + endpoint);
                System.out.println("Headers: ");
                System.out.println("  Content-Type: " + contentType);
                System.out.println("  Authorization: " + authType + " " + authToken);
                System.out.println("Body: ");
                System.out.println(httpBodies.get(i));
                
                // Here's where you would make the actual HTTP request in a real implementation
                // Example with JMeter DSL:
                // TestPlan testPlan = TestPlanStats.builder()
                //     .thread()
                //         .withHttpSampler("HTTP Request")
                //             .method(method)
                //             .domain(baseUrl)
                //             .path(endpoint)
                //             .header("Content-Type", contentType)
                //             .header("Authorization", authType + " " + authToken)
                //             .body(httpBodies.get(i))
                //         .end()
                //     .end()
                //     .build();
                // testPlan.run();
            }
            
            // Process GraphQL templates
            System.out.println("\n\nProcessing GraphQL templates:");
            List<String> graphqlQueries = processGraphQLTemplates(
                "src/test/resources/body/graphql/paginated_query_template.jinja",
                "src/test/resources/data/graphql/test_data.csv"
            );
            
            // Show how the templates would be used in an actual GraphQL request
            for (int i = 0; i < graphqlQueries.size(); i++) {
                Map<String, Object> requestData = parseCSV("src/test/resources/data/graphql/test_data.csv").get(i);
                String operationName = (String) requestData.get("operation_name");
                String id = (String) requestData.get("id");
                String limit = (String) requestData.get("limit");
                String offset = (String) requestData.get("offset");
                
                // Create the full GraphQL request
                System.out.println("\nGraphQL Request " + (i+1) + ":");
                System.out.println("Operation: " + operationName);
                System.out.println("Complete Request: ");
                System.out.println("{\n" +
                    "  \"query\": \"" + graphqlQueries.get(i).replace("\n", "\\n") + "\",\n" +
                    "  \"variables\": {\n" +
                    "    \"id\": " + id + ",\n" +
                    "    \"pagination\": {\n" +
                    "      \"limit\": " + limit + ",\n" +
                    "      \"offset\": " + offset + "\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"operationName\": \"" + operationName + "\"\n" +
                    "}");
                
                // Example of how to use this in a real GraphQL request
                // GraphQLRequestConfig config = new GraphQLRequestConfig();
                // config.setQuery(graphqlQueries.get(i));
                // config.setOperationName(operationName);
                // Map<String, Object> variables = new HashMap<>();
                // variables.put("id", Integer.parseInt(id));
                // // ... add other variables
                // config.setVariables(variables);
            }
            
            // Process SOAP templates
            System.out.println("\n\nProcessing SOAP templates:");
            List<String> soapBodies = processSoapTemplates(
                "src/test/resources/body/soap/auth_service_template.jinja",
                "src/test/resources/data/soap/test_data.csv"
            );
            
            // Show how the templates would be used in an actual SOAP request
            for (int i = 0; i < soapBodies.size(); i++) {
                Map<String, Object> requestData = parseCSV("src/test/resources/data/soap/test_data.csv").get(i);
                String xmlNamespace = (String) requestData.get("xml_namespace_uri");
                String xmlPrefix = (String) requestData.get("xml_namespace_prefix");
                String username = (String) requestData.get("username");
                String password = (String) requestData.get("password");
                
                // Create the full SOAP envelope
                System.out.println("\nSOAP Request " + (i+1) + ":");
                System.out.println("Namespace: " + xmlNamespace);
                System.out.println("Complete Envelope: ");
                System.out.println("<soapenv:Envelope \n" +
                    "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" \n" +
                    "    xmlns:" + xmlPrefix + "=\"" + xmlNamespace + "\">\n" +
                    "  <soapenv:Header>\n" +
                    "    <" + xmlPrefix + ":AuthHeader>\n" +
                    "      <" + xmlPrefix + ":Username>" + username + "</" + xmlPrefix + ":Username>\n" +
                    "      <" + xmlPrefix + ":Password>" + password + "</" + xmlPrefix + ":Password>\n" +
                    "    </" + xmlPrefix + ":AuthHeader>\n" +
                    "  </soapenv:Header>\n" +
                    "  <soapenv:Body>\n" +
                    "    " + soapBodies.get(i).replace("\n", "\n    ") + "\n" +
                    "  </soapenv:Body>\n" +
                    "</soapenv:Envelope>");
                
                // Example of how to use this in a real SOAP request
                // SoapRequestConfig config = new SoapRequestConfig();
                // config.setEndpoint("https://service.example.com/soap");
                // config.setSoapAction("http://example.com/GetUserDetails");
                // config.setEnvelope(soapEnvelope);
            }
            
            // Demonstrate using file references as defined in configuration files
            System.out.println("\n\nDemonstrating file reference processing:");
            
            // Example for GraphQL
            String graphqlQueryPath = "body/graphql/get_countries_by_continent.graphql";
            Map<String, Object> graphqlVariables = new HashMap<>();
            graphqlVariables.put("code", "EU");
            
            System.out.println("\nGraphQL file reference: " + graphqlQueryPath);
            String graphqlQueryContent = processConfigValue(graphqlQueryPath, graphqlVariables);
            System.out.println("Loaded and processed GraphQL query:\n" + graphqlQueryContent);
            
            // Example for SOAP
            String soapEnvelopePath = "body/soap/get_weather_request.xml";
            Map<String, Object> soapVariables = new HashMap<>();
            soapVariables.put("messageId", "msg-" + System.currentTimeMillis());
            
            System.out.println("\nSOAP file reference: " + soapEnvelopePath);
            String soapEnvelopeContent = processConfigValue(soapEnvelopePath, soapVariables);
            System.out.println("Loaded and processed SOAP envelope:\n" + soapEnvelopeContent);
            
        } catch (Exception e) {
            System.err.println("Error processing templates: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
