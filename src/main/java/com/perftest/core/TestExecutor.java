package com.perftest.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.perftest.assertions.AssertionManager;
import com.perftest.config.ExecutionConfig;
import com.perftest.config.GraphQLRequestConfig;
import com.perftest.config.RequestConfig;
import com.perftest.config.ScenarioConfig;
import com.perftest.config.SoapRequestConfig;
import com.perftest.config.SoapRequestConfig.XPathValidation;
import com.perftest.config.TestConfig;
import com.perftest.config.VariablesConfig;
import com.perftest.model.TestPlanStatsWrapper;
import com.perftest.utils.ErrorHandler;
import com.perftest.utils.JMeterInitializer;
import com.perftest.utils.JsonUtils;

import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * Core class responsible for executing JMeter tests based on the provided
 * configuration.
 */
public class TestExecutor {
    private static final Logger LOGGER = LogManager.getLogger(TestExecutor.class);
    private TestConfig testConfig;
    private final ResourceLoader resourceLoader;
    private final VariableResolver variableResolver;
    private final AssertionManager assertionManager;

    /**
     * Runs a simple HTTP test to verify that JMeter reports are correctly
     * generated.
     * This is a utility method that can be called to test the reporting
     * functionality.
     * 
     * @return TestPlanStatsWrapper containing the test results
     */
    public TestPlanStatsWrapper runReportTest() {
        try {
            // Create a simple test plan with a single HTTP request
            LOGGER.info("Running report generation test");

            // Create target directory for HTML reports
            File reportDir = new File("target/jmeter-reports");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            // Create a test plan with a simple HTTP GET request and HTML reporting
            DslTestPlan testPlan = JmeterDsl.testPlan(
                    JmeterDsl.threadGroup(1, 1,
                            JmeterDsl.httpSampler("Test Request", "https://httpbin.org/get")
                                    .method("GET")),
                    // Add HTML reporter to generate dashboard report
                    JmeterDsl.htmlReporter(reportDir.getPath()));

            // Run the test plan with HTML reporting
            LOGGER.info("Running test with JMeter's standard HTML report generation");
            LOGGER.info("Reports will be saved to {}", reportDir.getAbsolutePath());

            TestPlanStats stats = testPlan.run();

            // Log results
            long samplesCount = stats.overall().samplesCount();
            long errorsCount = stats.overall().errorsCount();
            double errorRate = samplesCount > 0 ? (double) errorsCount * 100 / samplesCount : 0;

            LOGGER.info("Test execution completed. Samples: {}, Errors: {}, Error Rate: {}%",
                    samplesCount,
                    errorsCount,
                    String.format("%.2f", errorRate));

            LOGGER.info("JMeter HTML reports generated in {}", reportDir.getAbsolutePath());
            LOGGER.info("You can view the reports by opening index.html in that directory");

            // Also validate the results
            validateResults(stats);

            // Return the test results
            return new TestPlanStatsWrapper(stats);
        } catch (Exception e) {
            LOGGER.error("Error running report test: {}", e.getMessage(), e);
            return new TestPlanStatsWrapper("Error running report test: " + e.getMessage());
        }
    }

    /**
     * Default constructor for cases when the config is loaded separately
     */
    public TestExecutor() {
        this.resourceLoader = new ResourceLoader();
        this.variableResolver = new VariableResolver();
        this.assertionManager = new AssertionManager();
    }

    /**
     * Constructs a TestExecutor with a pre-loaded test configuration
     * 
     * @param testConfig The test configuration
     */
    public TestExecutor(TestConfig testConfig) {
        this.testConfig = testConfig;
        this.resourceLoader = new ResourceLoader();
        this.variableResolver = new VariableResolver();
        this.assertionManager = new AssertionManager();
    }

    /**
     * Executes a test based on the provided configuration file.
     *
     * @param configFile Path to the test configuration file
     * @return True if the test was successful, false otherwise
     */
    public boolean execute(String configFilePath) {
        return ErrorHandler.executeSafely(() -> {
            LOGGER.info("Loading test configuration from: {}", configFilePath);

            // Try to load as a file path first
            File file = new File(configFilePath);

            // Final variable for use in lambda
            final String finalConfigPath;

            if (!file.exists()) {
                // If file doesn't exist at the direct path, try loading from resources
                String resourcePath = "src/test/resources/" + configFilePath;
                File resourceFile = new File(resourcePath);
                if (resourceFile.exists()) {
                    // Found in resources directory
                    LOGGER.info("Found configuration file in resources: {}", resourcePath);
                    finalConfigPath = resourcePath;
                } else {
                    LOGGER.error("Test configuration file not found at: {} or {}", configFilePath, resourcePath);
                    return false;
                }
            } else {
                finalConfigPath = configFilePath;
            }

            // Load the test configuration with the final path
            this.testConfig = loadTestConfig(finalConfigPath);
            if (this.testConfig == null) {
                return false;
            }

            // Execute the test
            try {
                TestPlanStats stats = executeTestPlan();

                // Handle null stats (returned when there's an error during test execution)
                if (stats == null) {
                    LOGGER.error("Test execution failed - no valid test statistics available");
                    return false;
                }

                // Validate the results
                validateResults(stats);

                LOGGER.info("Test execution completed successfully");
                return true;
            } catch (Exception e) {
                // Handle errors
                LOGGER.error("Error during test execution: {}", e.getMessage());
                return false;
            }
        }, "Error executing test", false);
    }

    /**
     * Loads a test configuration from a file.
     *
     * @param configFile The path to the configuration file
     * @return The loaded TestConfig, or null if loading failed
     */
    private TestConfig loadTestConfig(String configFile) {
        return ErrorHandler.executeSafely(() -> {
            String configContent = new String(Files.readAllBytes(Paths.get(configFile)));
            TestConfig config = TestConfig.fromYaml(configContent);
            LOGGER.info("Loaded test configuration with {} scenario(s)", config.getScenarios().size());
            return config;
        }, "Failed to load test configuration", null);
    }

    /**
     * Executes the test plan based on the loaded configuration.
     *
     * @return The test execution statistics
     * @throws Exception If any error occurs during execution
     */
    private TestPlanStats executeTestPlan() throws Exception {
        LOGGER.info("Starting test execution with {} scenario(s)", testConfig.getScenarios().size());

        // Initialize JMeter environment before starting test execution
        JMeterInitializer.initialize();

        // Based on the working SimpleJMeterExample, we need to build the test plan
        // differently
        // We'll collect all thread groups first
        List<DslThreadGroup> threadGroups = new ArrayList<>();

        // Create thread groups for each scenario
        for (ScenarioConfig scenario : testConfig.getScenarios()) {
            // Set up variables for this scenario
            VariablesConfig variables = new VariablesConfig();
            variables.setGlobalVariables(
                    testConfig.getVariables() != null ? testConfig.getVariables() : new HashMap<>());

            if (testConfig.getExecution() != null && testConfig.getExecution().getVariables() != null) {
                variables.setExecutionVariables(testConfig.getExecution().getVariables());
            }

            if (scenario.getVariables() != null) {
                variables.setScenarioVariables(scenario.getVariables());
            }

            // Build samplers for the scenario
            List<DslHttpSampler> samplers = buildSamplers(scenario, variables);

            // Get execution config
            ExecutionConfig execution = testConfig.getExecution();

            // Create a thread group for this scenario
            // Using JmeterDsl.threadGroup() directly as in the SimpleJMeterExample
            DslThreadGroup threadGroup = JmeterDsl.threadGroup(
                    execution.getThreads(),
                    execution.getIterations(),
                    samplers.toArray(new DslHttpSampler[0]));

            threadGroups.add(threadGroup);
        }

        // Create array from thread groups
        DslThreadGroup[] threadGroupArray = threadGroups.toArray(new DslThreadGroup[0]);

        return executeTestPlan(threadGroupArray);
    }

    /**
     * Executes the test plan with the given thread groups.
     *
     * @param threadGroupArray Array of thread groups to execute
     * @return The test execution statistics
     */
    private TestPlanStats executeTestPlan(DslThreadGroup[] threadGroupArray) {
        TestPlanStats stats = null;

        try {
            // Create target directory for JMeter reports if it doesn't exist
            File reportDir = new File("target/jmeter-reports");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            // Create and run the test plan with reporting features
            DslTestPlan testPlan;

            // Prepare test plan elements with HTML report generation
            if (threadGroupArray.length == 0) {
                // Empty test plan with HTML report
                testPlan = JmeterDsl.testPlan(
                        JmeterDsl.htmlReporter(reportDir.getPath()));
            } else if (threadGroupArray.length == 1) {
                // Single thread group with HTML report
                testPlan = JmeterDsl.testPlan(
                        threadGroupArray[0],
                        JmeterDsl.htmlReporter(reportDir.getPath()));
            } else {
                // Multiple thread groups with HTML report
                List<DslTestPlan.TestPlanChild> testPlanElements = new ArrayList<>();
                // Add all thread groups
                for (DslThreadGroup threadGroup : threadGroupArray) {
                    testPlanElements.add(threadGroup);
                }
                // Add HTML reporter
                testPlanElements.add(JmeterDsl.htmlReporter(reportDir.getPath()));

                testPlan = JmeterDsl.testPlan(
                        testPlanElements.toArray(new DslTestPlan.TestPlanChild[0]));
            }

            LOGGER.info("Running test with JMeter's standard HTML report generation");
            LOGGER.info("Reports will be saved to {}", reportDir.getAbsolutePath());

            // Run the test plan - this will automatically generate HTML reports
            stats = testPlan.run();

            // Log the results
            long samplesCount = stats.overall().samplesCount();
            long errorsCount = stats.overall().errorsCount();
            double errorRate = samplesCount > 0 ? (double) errorsCount * 100 / samplesCount : 0;

            LOGGER.info("Test execution completed. Samples: {}, Errors: {}, Error Rate: {}%",
                    samplesCount,
                    errorsCount,
                    String.format("%.2f", errorRate));

            LOGGER.info("JMeter HTML reports generated in {}", reportDir.getAbsolutePath());
            LOGGER.info("You can view the reports by opening index.html in that directory");
        } catch (Exception e) {
            LOGGER.error("Error during test execution: {}", e.getMessage(), e);
            return null;
        }

        validateResults(stats);
        return stats;
    }

    /**
     * Builds a scenario and adds it to the test plan.
     *
     * @param testPlan The test plan to add to
     * @param scenario The scenario configuration
     * @throws Exception If any error occurs during scenario creation
     */
    // private void buildScenario(DslTestPlan testPlan, ScenarioConfig scenario)
    // throws Exception {
    // LOGGER.info("Building scenario: {}", scenario.getName());

    // // Set up the variables for this scenario
    // VariablesConfig variables = new VariablesConfig();
    // variables.setGlobalVariables(testConfig.getVariables() != null ?
    // testConfig.getVariables() : new HashMap<>());

    // if (testConfig.getExecution() != null &&
    // testConfig.getExecution().getVariables() != null) {
    // variables.setExecutionVariables(testConfig.getExecution().getVariables());
    // }

    // if (scenario.getVariables() != null) {
    // variables.setScenarioVariables(scenario.getVariables());
    // }

    // // Create the HTTP samplers for the scenario
    // List<DslHttpSampler> samplers = new ArrayList<>();

    // for (RequestConfig request : scenario.getRequests()) {
    // // Set request-specific variables
    // VariablesConfig requestVars = new VariablesConfig();
    // requestVars.setGlobalVariables(variables.getGlobalVariables());
    // requestVars.setExecutionVariables(variables.getExecutionVariables());
    // requestVars.setScenarioVariables(variables.getScenarioVariables());

    // if (request.getVariables() != null) {
    // requestVars.setRequestVariables(request.getVariables());
    // }

    // // Create the HTTP sampler
    // DslHttpSampler sampler = buildHttpSampler(request, requestVars);
    // samplers.add(sampler);
    // }
    // }

    /**
     * Builds JMeter samplers for all requests in a scenario.
     *
     * @param scenario  The scenario configuration
     * @param variables The variables configuration
     * @return A list of configured JMeter samplers
     * @throws Exception If any error occurs during sampler creation
     */
    private List<DslHttpSampler> buildSamplers(ScenarioConfig scenario, VariablesConfig variables) throws Exception {
        List<DslHttpSampler> samplers = new ArrayList<>();

        // Handle standard HTTP requests
        if (scenario.getRequests() != null && !scenario.getRequests().isEmpty()) {
            for (RequestConfig request : scenario.getRequests()) {
                // Set request-specific variables
                VariablesConfig requestVars = new VariablesConfig();
                requestVars.setGlobalVariables(variables.getGlobalVariables());
                requestVars.setExecutionVariables(variables.getExecutionVariables());
                requestVars.setScenarioVariables(variables.getScenarioVariables());

                if (request.getVariables() != null) {
                    requestVars.setRequestVariables(request.getVariables());
                }

                // Create the HTTP sampler
                DslHttpSampler sampler = buildHttpSampler(request, requestVars);
                samplers.add(sampler);
            }
        }

        // Handle GraphQL requests
        if (scenario.getGraphQLRequests() != null && !scenario.getGraphQLRequests().isEmpty()) {
            for (GraphQLRequestConfig request : scenario.getGraphQLRequests()) {
                // Set request-specific variables
                VariablesConfig requestVars = new VariablesConfig();
                requestVars.setGlobalVariables(variables.getGlobalVariables());
                requestVars.setExecutionVariables(variables.getExecutionVariables());
                requestVars.setScenarioVariables(variables.getScenarioVariables());

                if (request.getVariables() != null) {
                    requestVars.setRequestVariables(request.getVariables());
                }

                // Create the GraphQL sampler
                DslHttpSampler sampler = buildGraphQLSampler(request, requestVars);
                samplers.add(sampler);
            }
        }

        // Handle SOAP requests
        if (scenario.getSoapRequests() != null && !scenario.getSoapRequests().isEmpty()) {
            for (SoapRequestConfig request : scenario.getSoapRequests()) {
                // Set request-specific variables
                VariablesConfig requestVars = new VariablesConfig();
                requestVars.setGlobalVariables(variables.getGlobalVariables());
                requestVars.setExecutionVariables(variables.getExecutionVariables());
                requestVars.setScenarioVariables(variables.getScenarioVariables());

                if (request.getVariables() != null) {
                    requestVars.setRequestVariables(request.getVariables());
                }

                // Create the SOAP sampler
                DslHttpSampler sampler = buildSoapSampler(request, requestVars);
                samplers.add(sampler);
            }
        }

        return samplers;
    }

    /**
     * Builds a JMeter HTTP sampler for a specific request.
     *
     * @param request   The request configuration
     * @param variables The variables configuration for this request
     * @return A configured JMeter HTTP sampler
     * @throws Exception If any error occurs during sampler creation
     */
    private DslHttpSampler buildHttpSampler(RequestConfig request, VariablesConfig variables) throws Exception {
        LOGGER.info("Building HTTP sampler for request: {}", request.getName());

        // Resolve variables in the endpoint
        String resolvedEndpoint = variableResolver.resolveVariables(request.getEndpoint(),
                variables.getMergedVariables());

        // Create the basic HTTP sampler
        DslHttpSampler sampler = JmeterDsl.httpSampler(request.getName(), resolvedEndpoint)
                .method(request.getMethod());

        // Add headers if specified
        if (request.getHeaders() != null) {
            Map<String, String> loadedHeaders = loadHeaders(request.getHeaders());
            if (loadedHeaders != null && !loadedHeaders.isEmpty()) {
                // Apply each header individually with the updated API
                for (Map.Entry<String, String> header : loadedHeaders.entrySet()) {
                    sampler = sampler.header(header.getKey(), header.getValue());
                }
            }
        }

        // Add request body if specified
        if (request.getBody() != null && (request.getMethod().equalsIgnoreCase("POST") ||
                request.getMethod().equalsIgnoreCase("PUT") ||
                request.getMethod().equalsIgnoreCase("PATCH"))) {
            String body = loadRequestBody(request.getBody());
            String resolvedBody = variableResolver.resolveVariables(body, variables.getMergedVariables());
            sampler = sampler.body(resolvedBody);
        }

        // Add parameters if specified
        if (request.getParams() != null) {
            Map<String, String> params = loadParameters(request.getParams(), variables.getMergedVariables());
            for (Map.Entry<String, String> param : params.entrySet()) {
                sampler = sampler.param(param.getKey(), param.getValue());
            }
        }

        // Add status code assertion if specified
        if (request.getStatusCode() != null) {
            LOGGER.info("Adding status code validation for endpoint: {} method: {} expected status code: {}",
                    resolvedEndpoint, request.getMethod(), request.getStatusCode());

            int statusCode = request.getStatusCode();

            // Use the AssertionManager to create a status code assertion based on
            // https://abstracta.github.io/jmeter-java-dsl/guide/#change-sample-result-statuses-with-custom-logic
            String parameters = "{\"expected\": " + statusCode + ", \"failOnError\": true}";
            DslJsr223PostProcessor statusCodeAssertion = assertionManager.createAssertion("statusCode", parameters);

            // Add the assertion to the sampler
            sampler = sampler.children(statusCodeAssertion);

            // Log the assertion creation with the fully resolved endpoint URL

            // Also add a response time assertion if needed
            if (request.getResponseTimeThreshold() > 0) {
                String timeParams = "{\"maxTime\": " + request.getResponseTimeThreshold() + "}";
                DslJsr223PostProcessor timeAssertion = assertionManager.createAssertion("responseTime", timeParams);
                sampler = sampler.children(timeAssertion);
            }

            // Add detailed logging about what we expect
            LOGGER.info("Added response code assertion for endpoint: {}. Expecting status code: {}",
                    resolvedEndpoint, statusCode);
        } else {
            LOGGER.info("No status code validation specified for endpoint: {}", resolvedEndpoint);
        }

        // Add response assertions if specified
        if (request.getResponses() != null && !request.getResponses().isEmpty()) {
            sampler = addResponseAssertions(sampler, request.getResponses());
        }

        return sampler;
    }

    /**
     * Builds a JMeter HTTP sampler for a SOAP request.
     *
     * @param request   The SOAP request configuration
     * @param variables The variables configuration for this request
     * @return A configured JMeter HTTP sampler for SOAP
     * @throws Exception If any error occurs during sampler creation
     */
    private DslHttpSampler buildSoapSampler(SoapRequestConfig request, VariablesConfig variables) throws Exception {
        LOGGER.info("Building SOAP sampler for request: {}", request.getName());

        // Resolve variables in the endpoint
        String resolvedEndpoint = variableResolver.resolveVariables(request.getEndpoint(),
                variables.getMergedVariables());

        // Create the basic HTTP sampler - SOAP uses HTTP POST
        DslHttpSampler sampler = JmeterDsl.httpSampler(request.getName(), resolvedEndpoint)
                .method("POST");

        // Add headers if specified
        if (request.getHeaders() != null) {
            Map<String, String> loadedHeaders = loadHeaders(request.getHeaders());
            if (loadedHeaders != null && !loadedHeaders.isEmpty()) {
                // Apply each header individually
                for (Map.Entry<String, String> header : loadedHeaders.entrySet()) {
                    sampler = sampler.header(header.getKey(), header.getValue());
                }
            }
        } else {
            // Add default SOAP headers if none specified
            sampler = sampler.header("Content-Type", "text/xml;charset=UTF-8");
            sampler = sampler.header("SOAPAction", request.getSoapAction() != null ? request.getSoapAction() : "");
        }

        // Add SOAP body if specified - use the correct field name from the class
        if (request.getSoapEnvelope() != null || request.getTemplateFile() != null) {
            // Use either soapEnvelope or templateFile, prioritizing soapEnvelope
            String templateContent = request.getSoapEnvelope() != null ? request.getSoapEnvelope()
                    : loadRequestBody(request.getTemplateFile());

            // Resolve variables in the SOAP envelope
            if (templateContent != null && !templateContent.isEmpty()) {
                String resolvedSoapBody = variableResolver.resolveVariables(templateContent,
                        variables.getMergedVariables());
                sampler = sampler.body(resolvedSoapBody);
            } else {
                LOGGER.warn("No valid SOAP envelope template found for request: {}", request.getName());
            }
        }

        // Handle XML response validation
        if (request.getStatusCode() != null) {
            LOGGER.info("Adding status code validation for SOAP endpoint: {} expected status code: {}",
                    resolvedEndpoint, request.getStatusCode());

            int statusCode = request.getStatusCode();

            // Use the AssertionManager to create a status code assertion
            String parameters = "{\"expected\": " + statusCode + ", \"failOnError\": true}";
            DslJsr223PostProcessor statusCodeAssertion = assertionManager.createAssertion("statusCode", parameters);

            // Add the assertion to the sampler
            sampler = sampler.children(statusCodeAssertion);

            // Log the assertion creation

            // Also add a response time assertion if needed
            if (request.getResponseTimeThreshold() > 0) {
                String timeParams = "{\"maxTime\": " + request.getResponseTimeThreshold() + "}";
                DslJsr223PostProcessor timeAssertion = assertionManager.createAssertion("responseTime", timeParams);
                sampler = sampler.children(timeAssertion);
            }
        }

        // Add XPath validations if specified
        if (request.getXpath() != null && !request.getXpath().isEmpty()) {
            for (XPathValidation xpathValidation : request.getXpath()) {
                // Create XPath assertion using AssertionManager
                String xpathParams = "{\"xpath\": \"" + xpathValidation.getExpression() + "\", " +
                        "\"expected\": \"" + xpathValidation.getExpected() + "\"}";

                try {
                    DslJsr223PostProcessor xpathAssertion = assertionManager.createAssertion("xpath", xpathParams);
                    sampler = sampler.children(xpathAssertion);
                } catch (Exception e) {
                    LOGGER.error("Failed to add XPath validation: {}", e.getMessage(), e);
                }
            }
        }

        return sampler;
    }

    /**
     * Builds a JMeter HTTP sampler for a GraphQL request.
     *
     * @param request   The GraphQL request configuration
     * @param variables The variables configuration for this request
     * @return A configured JMeter HTTP sampler for GraphQL
     * @throws Exception If any error occurs during sampler creation
     */
    private DslHttpSampler buildGraphQLSampler(GraphQLRequestConfig request, VariablesConfig variables)
            throws Exception {
        LOGGER.info("Building GraphQL sampler for request: {}", request.getName());

        // Resolve variables in the endpoint
        String resolvedEndpoint = variableResolver.resolveVariables(request.getEndpoint(),
                variables.getMergedVariables());

        // Create the basic HTTP sampler - GraphQL uses HTTP POST
        DslHttpSampler sampler = JmeterDsl.httpSampler(request.getName(), resolvedEndpoint)
                .method("POST");

        // Add Content-Type header for GraphQL
        sampler = sampler.header("Content-Type", "application/json");

        // Load GraphQL query and variables
        String queryContent = null;

        // First check for queryFile property
        if (request.getQueryFile() != null) {
            // Load from the queryFile property
            String queryFile = request.getQueryFile().trim();
            LOGGER.info("Loading GraphQL query from file: {}", queryFile);
            queryContent = loadRequestBody(queryFile);
        }
        // Then check for query property (backwards compatibility)
        else if (request.getQuery() != null) {
            // Determine if this is a file path or an inline query
            String graphqlQuery = request.getQuery().trim();

            if (graphqlQuery.startsWith("templates/") || graphqlQuery.startsWith("src/test/resources/")) {
                // This is a file path, load the template from file
                LOGGER.info("Loading GraphQL query from file: {}", graphqlQuery);
                queryContent = loadRequestBody(graphqlQuery);
            } else {
                // This is an inline query
                LOGGER.info("Using inline GraphQL query");
                queryContent = graphqlQuery;
            }
        }

        if (queryContent != null) {
            // Create the GraphQL request JSON
            Map<String, Object> graphqlRequest = new HashMap<>();
            graphqlRequest.put("query", queryContent);

            // Add variables if they exist
            if (request.getGraphQLVariables() != null && !request.getGraphQLVariables().isEmpty()) {
                // First resolve any template variables in the GraphQL variables
                Map<String, Object> resolvedGraphQLVars = new HashMap<>();

                for (Map.Entry<String, Object> entry : request.getGraphQLVariables().entrySet()) {
                    if (entry.getValue() instanceof String) {
                        String resolvedValue = variableResolver.resolveVariables(
                                (String) entry.getValue(), variables.getMergedVariables());
                        resolvedGraphQLVars.put(entry.getKey(), resolvedValue);
                    } else {
                        // Keep as is if not a string (e.g., numbers, boolean, nested objects)
                        resolvedGraphQLVars.put(entry.getKey(), entry.getValue());
                    }
                }

                // Cast is needed since the Map type is expected to be <String, Object>
                graphqlRequest.put("variables", resolvedGraphQLVars);
            }

            // Convert to JSON
            String jsonBody = JsonUtils.toJson(graphqlRequest);
            sampler = sampler.body(jsonBody);
        }

        // Add assertions for GraphQL
        if (request.getStatusCode() != null) {
            LOGGER.info("Adding status code validation for GraphQL endpoint: {} expected status code: {}",
                    resolvedEndpoint, request.getStatusCode());

            int statusCode = request.getStatusCode();

            // Use the AssertionManager to create a status code assertion
            String parameters = "{\"expected\": " + statusCode + ", \"failOnError\": true}";
            DslJsr223PostProcessor statusCodeAssertion = assertionManager.createAssertion("statusCode", parameters);

            // Add the assertion to the sampler
            sampler = sampler.children(statusCodeAssertion);

            // Log the assertion creation

            // Also add a response time assertion if needed
            if (request.getResponseTimeThreshold() > 0) {
                String timeParams = "{\"maxTime\": " + request.getResponseTimeThreshold() + "}";
                DslJsr223PostProcessor timeAssertion = assertionManager.createAssertion("responseTime", timeParams);
                sampler = sampler.children(timeAssertion);
            }

            // Add a GraphQL-specific check that is simpler - just verify there's no
            // "errors" in the response
            String jsScript = "var responseData = prev.getResponseDataAsString();\n" +
                    "if (responseData.indexOf('\"errors\"') !== -1) {\n" +
                    "    log.error(\"GraphQL response contains errors\");\n" +
                    "    prev.setSuccessful(false);\n" +
                    "    prev.setResponseMessage(\"GraphQL response contains errors\");\n" +
                    "} else {\n" +
                    "    log.info(\"GraphQL validation passed: No errors in response\");\n" +
                    "}\n";

            // Create a custom processor with explicit JavaScript engine specification
            DslJsr223PostProcessor noErrorsAssertion = assertionManager.createAssertion("contains",
                    "{\"text\": \"data\", \"failOnError\": true}");

            // If that doesn't work, create a fallback with direct instantiation
            if (noErrorsAssertion == null) {
                LOGGER.warn("Creating fallback JavaScript processor for GraphQL error detection");
                noErrorsAssertion = new DslJsr223PostProcessor("javascript", jsScript);
            }
            sampler = sampler.children(noErrorsAssertion);
        }

        return sampler;
    }

    /**
     * Adds response assertions to a sampler.
     *
     * @param sampler   The sampler to add assertions to
     * @param responses The response assertion configuration
     * @return The sampler with assertions added
     */
    private DslHttpSampler addResponseAssertions(DslHttpSampler sampler, Map<String, Object> responses) {
        // Handle "Contains" assertions
        if (responses.containsKey("Contains") && responses.get("Contains") != null) {
            try {
                LOGGER.info("Adding response content validation: {}", responses.get("Contains"));
                String containsParams = "{\"text\": \"" + responses.get("Contains").toString() + "\"}";
                DslJsr223PostProcessor containsAssertion = assertionManager.createAssertion("contains", containsParams);
                sampler = sampler.children(containsAssertion);
            } catch (Exception e) {
                LOGGER.error("Failed to add content validation: {}", e.getMessage(), e);
            }
        }

        // Handle JSONPath assertions
        if (responses.containsKey("JsonPath") && responses.get("JsonPath") != null) {
            try {
                List<String> jsonPathSpecs = getJsonPathSpecs(responses.get("JsonPath"));
                for (String jsonPathSpec : jsonPathSpecs) {
                    String[] parts = jsonPathSpec.split("=", 2);
                    if (parts.length == 2) {
                        String path = parts[0].trim();
                        String expected = parts[1].trim();
                        LOGGER.info("Adding JSONPath validation - path: {}, expected: {}", path, expected);
                        String jsonPathParams = "{\"path\": \"" + path + "\", \"expected\": \"" + expected + "\"}";
                        DslJsr223PostProcessor jsonPathAssertion = assertionManager.createAssertion("jsonPath",
                                jsonPathParams);
                        sampler = sampler.children(jsonPathAssertion);
                    } else {
                        LOGGER.warn("Invalid JsonPath format: {}, expected format is 'path=value'", jsonPathSpec);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to add JSONPath validation: {}", e.getMessage(), e);
            }
        }

        // Handle schema validation - this is more complex and may need a separate
        // processor
        if (responses.containsKey("Schema") && responses.get("Schema") != null) {
            LOGGER.info("Response schema validation noted: {}", responses.get("Schema").toString());
            // TODO: Implement schema validation if required
        }

        return sampler;
    }

    /**
     * Extract JsonPath specifications from configuration.
     *
     * @param config The JsonPath configuration - could be a String or a List
     * @return List of JsonPath specifications
     */
    @SuppressWarnings("unchecked")
    private List<String> getJsonPathSpecs(Object config) {
        List<String> specs = new ArrayList<>();

        if (config instanceof String) {
            // Single JsonPath spec
            specs.add((String) config);
        } else if (config instanceof List) {
            // List of JsonPath specs
            specs.addAll((List<String>) config);
        }

        return specs;
    }

    /**
     * Validates the test results against thresholds.
     *
     * @param stats The test execution statistics
     */
    private void validateResults(TestPlanStats stats) {
        if (stats == null) {
            LOGGER.warn("No test statistics available for validation");
            return;
        }

        try {
            // Get overall statistics using the available methods in TestPlanStats
            long totalSamples = 0;
            long totalErrors = 0;

            // Use the available overall() method and its metrics
            var overallStats = stats.overall();
            if (overallStats != null) {
                // Get sample count using available methods
                totalSamples = overallStats.samplesCount();
                // Calculate errors from error count
                totalErrors = overallStats.errorsCount();
            }

            // Calculate error rate
            double errorRate = (totalSamples > 0) ? (totalErrors * 100.0 / totalSamples) : 0;

            // Log statistics
            LOGGER.info("Overall results - Samples: {}, Errors: {}, Error Rate: {}%",
                    totalSamples, totalErrors, String.format("%.2f", errorRate));

            // Add response time statistics
            try {
                LOGGER.info("Response Time Statistics:");
                LOGGER.info("- Average: {} ms", String.format("%.2f", overallStats.sampleTime().mean()));
                LOGGER.info("- Median: {} ms", String.format("%.2f", overallStats.sampleTime().median()));
                LOGGER.info("- 90th Percentile: {} ms", String.format("%.2f", overallStats.sampleTime().perc90()));
                LOGGER.info("- 95th Percentile: {} ms", String.format("%.2f", overallStats.sampleTime().perc95()));
                LOGGER.info("- 99th Percentile: {} ms", String.format("%.2f", overallStats.sampleTime().perc99()));
                LOGGER.info("- Min: {} ms", String.format("%.2f", overallStats.sampleTime().min()));
                LOGGER.info("- Max: {} ms", String.format("%.2f", overallStats.sampleTime().max()));
            } catch (Exception e) {
                LOGGER.warn("Could not calculate all response time statistics: {}", e.getMessage());
            }

            // Validate against default threshold of 0%
            double errorRateThreshold = 0.0;

            LOGGER.info("Test execution completed. Success rate: {}%, Threshold: {}%",
                    String.format("%.2f", 100.0 - errorRate), String.format("%.2f", 100.0 - errorRateThreshold));

            // Log the success/failure message
            if (errorRate <= errorRateThreshold) {
                LOGGER.info("✅ Test PASSED - Error rate of {}% is within the acceptable threshold of {}%",
                        String.format("%.2f", errorRate), String.format("%.2f", errorRateThreshold));
            } else {
                LOGGER.error("❌ Test FAILED - Error rate of {}% exceeds the acceptable threshold of {}%",
                        String.format("%.2f", errorRate), String.format("%.2f", errorRateThreshold));
            }

            // Generate a report file with detailed statistics
            try {
                File reportDir = new File("target/jmeter-reports");
                if (!reportDir.exists()) {
                    reportDir.mkdirs();
                }

                // Create a detailed report
                StringBuilder reportContent = new StringBuilder();
                reportContent.append("# JMeter Test Execution Report\n\n");
                reportContent.append("## Overall Statistics\n\n");
                reportContent.append(String.format("- **Total Samples**: %d\n", totalSamples));
                reportContent.append(String.format("- **Error Count**: %d\n", totalErrors));
                reportContent.append(String.format("- **Error Rate**: %.2f%%\n", errorRate));
                reportContent.append(String.format("- **Success Rate**: %.2f%%\n", 100.0 - errorRate));
                reportContent.append("\n## Response Time Statistics\n\n");
                reportContent.append(String.format("- **Average**: %.2f ms\n", overallStats.sampleTime().mean()));
                reportContent.append(String.format("- **Median**: %.2f ms\n", overallStats.sampleTime().median()));
                reportContent
                        .append(String.format("- **90th Percentile**: %.2f ms\n", overallStats.sampleTime().perc90()));
                reportContent
                        .append(String.format("- **95th Percentile**: %.2f ms\n", overallStats.sampleTime().perc95()));
                reportContent
                        .append(String.format("- **99th Percentile**: %.2f ms\n", overallStats.sampleTime().perc99()));
                reportContent.append(String.format("- **Min**: %.2f ms\n", overallStats.sampleTime().min()));
                reportContent.append(String.format("- **Max**: %.2f ms\n", overallStats.sampleTime().max()));

                // Add per-label statistics
                reportContent.append("\n## Results by Label\n\n");
                for (String label : stats.labels()) {
                    var labelStats = stats.byLabel(label);
                    reportContent.append(String.format("### %s\n\n", label));
                    reportContent.append(String.format("- **Samples**: %d\n", labelStats.samplesCount()));
                    reportContent.append(String.format("- **Errors**: %d\n", labelStats.errorsCount()));
                    double labelErrorRate = labelStats.samplesCount() > 0
                            ? (labelStats.errorsCount() * 100.0 / labelStats.samplesCount())
                            : 0;
                    reportContent.append(String.format("- **Error Rate**: %.2f%%\n", labelErrorRate));
                    reportContent
                            .append(String.format("- **Average Time**: %.2f ms\n", labelStats.sampleTime().mean()));
                    reportContent
                            .append(String.format("- **Median Time**: %.2f ms\n", labelStats.sampleTime().median()));
                    reportContent.append(
                            String.format("- **90th Percentile**: %.2f ms\n", labelStats.sampleTime().perc90()));
                    reportContent.append(
                            String.format("- **95th Percentile**: %.2f ms\n", labelStats.sampleTime().perc95()));
                    reportContent.append(
                            String.format("- **99th Percentile**: %.2f ms\n", labelStats.sampleTime().perc99()));
                    reportContent.append("\n");
                }

                // Add the validation result
                reportContent.append("## Test Result\n\n");
                if (errorRate <= errorRateThreshold) {
                    reportContent.append(String.format(
                            "✅ **PASSED** - Error rate of %.2f%% is within the acceptable threshold of %.2f%%\n",
                            errorRate, errorRateThreshold));
                } else {
                    reportContent.append(String.format(
                            "❌ **FAILED** - Error rate of %.2f%% exceeds the acceptable threshold of %.2f%%\n",
                            errorRate, errorRateThreshold));
                }

                // Write the report to a file
                File reportFile = new File(reportDir, "test-report.md");
                Files.write(reportFile.toPath(), reportContent.toString().getBytes());
                LOGGER.info("Detailed report saved to: {}", reportFile.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.warn("Failed to generate detailed report file: {}", e.getMessage());
            }
        } catch (Exception e) {
            // Log but don't fail if statistics processing has an error
            LOGGER.error("Error processing statistics: {}", e.getMessage());
        }
    }

    /**
     * Loads headers from a YAML file or inline content.
     * 
     * @param headers The headers specification (file path or inline content)
     * @return The loaded headers
     */
    private Map<String, String> loadHeaders(Object headers) {
        if (headers == null) {
            return Collections.emptyMap();
        }

        // If headers is already a Map, return it directly
        if (headers instanceof Map) {
            Map<String, String> result = new HashMap<>();
            Map<String, Object> headerMap = (Map<String, Object>) headers;

            // Convert all values to String
            for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                result.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
            }

            return result;
        }

        // Otherwise, treat it as a file path
        String headersPath = headers.toString();
        try {
            String headersContent = resourceLoader.loadResource(headersPath);
            LOGGER.debug("Loading headers from file: {}", headers);

            // Parse the YAML content manually
            if (headersContent != null && !headersContent.isEmpty()) {
                Yaml yaml = new Yaml();
                Map<String, Object> parsedHeaders = yaml.load(headersContent);

                // Convert to Map<String, String> since that's what our method expects
                Map<String, String> stringHeaders = new HashMap<>();
                if (parsedHeaders != null) {
                    for (Map.Entry<String, Object> entry : parsedHeaders.entrySet()) {
                        stringHeaders.put(entry.getKey(),
                                entry.getValue() != null ? entry.getValue().toString() : "");
                    }
                }

                return stringHeaders;
            }

            return Collections.emptyMap();
        } catch (IOException e) {
            LOGGER.error("Failed to load headers from file: {}", headers, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Loads a request body from a template file or inline content.
     * 
     * @param body The body specification (file path or inline content)
     * @return The loaded body content
     */
    private String loadRequestBody(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }

        // If it doesn't look like a file path (e.g., contains JSON or XML syntax
        // markers),
        // treat it as inline content
        if (body.trim().startsWith("{") || body.trim().startsWith("<") ||
                body.trim().startsWith("query") || body.trim().startsWith("mutation")) {
            LOGGER.debug("Using direct body content (not loading from file)");
            return body;
        }

        try {
            LOGGER.info("Attempting to load body content from file: {}", body);
            String bodyContent = resourceLoader.loadResource(body);

            if (bodyContent == null || bodyContent.isEmpty()) {
                LOGGER.warn("Body content loaded from file '{}' is empty", body);
                return "";
            }

            LOGGER.debug("Successfully loaded body content from file: {}", body);

            // For both GraphQL and SOAP, we just want to use the content directly
            // and not process it through different template engines
            return bodyContent;
        } catch (IOException e) {
            LOGGER.error("Failed to load body from file: {} - {}", body, e.getMessage());
            // Return empty string instead of null to avoid NPEs
            return "";
        }
    }

    /**
     * Loads parameters from a file or configuration and resolves variables.
     * 
     * @param params    The parameters specification (file path or inline content)
     * @param variables The variables to resolve in the parameters
     * @return The loaded parameters
     */
    private Map<String, String> loadParameters(Object params, Map<String, Object> variables) {
        if (params == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();

        // If params is already a Map, process it directly
        if (params instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> paramsMap = (Map<String, ?>) params;

            for (Map.Entry<String, ?> entry : paramsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Convert value to string and resolve variables
                String stringValue = (value != null) ? value.toString() : "";
                // Make sure we're using Map<String, String> for result
                result.put(key, variableResolver.resolveVariables(stringValue, variables));
            }

            return result;
        }

        // Otherwise, treat it as a file path
        String paramsPath = params.toString();
        try {
            String paramsContent = resourceLoader.loadResource(paramsPath);
            LOGGER.debug("Loading parameters from file: {}", params);

            // Parse the content as YAML
            if (paramsContent != null && !paramsContent.isEmpty()) {
                // Parse the YAML manually
                Yaml yaml = new Yaml();
                Map<String, Object> loadedParams = yaml.load(paramsContent);

                if (loadedParams != null) {
                    // Process and resolve variables in the loaded parameters
                    for (Map.Entry<String, Object> entry : loadedParams.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();

                        // Convert value to string and resolve variables
                        String stringValue = (value != null) ? value.toString() : "";
                        result.put(key, variableResolver.resolveVariables(stringValue, variables));
                    }
                }
            }

            return result;
        } catch (IOException e) {
            LOGGER.error("Failed to load parameters from file: {}", params, e);
            return Collections.emptyMap();
        }
    }
}