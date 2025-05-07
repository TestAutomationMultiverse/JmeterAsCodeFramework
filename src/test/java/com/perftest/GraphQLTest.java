package com.perftest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perftest.config.TestConfig;
import com.perftest.core.ConfigLoader;
import com.perftest.core.TestExecutor;

/**
 * Test class for running GraphQL performance tests against the Countries
 * GraphQL API.
 * <p>
 * This test demonstrates how to use the JMeter DSL Framework to test GraphQL
 * APIs by sending
 * various types of GraphQL queries, including:
 * <ul>
 * <li>Querying a single country with variables</li>
 * <li>Filtering countries by continent</li>
 * <li>Fetching all continents with nested country data</li>
 * </ul>
 * <p>
 * The Countries GraphQL API (https://countries.trevorblades.com/) is used as
 * the test endpoint
 * since it's publicly available and doesn't require authentication.
 */
public class GraphQLTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLTest.class);
    private static final String CONFIG_FILE = "src/test/resources/configs/graphql_test_config.yaml";

    @BeforeAll
    public static void setup() {
        // This method can be used for test-wide setup if needed
        LOGGER.info("Setting up GraphQL performance test");
    }

    /**
     * Executes a performance test against the Countries GraphQL API.
     * <p>
     * This test loads configuration from the YAML file, which defines multiple
     * GraphQL queries including filtering data and querying nested structures.
     * <p>
     * The test demonstrates:
     * - Basic GraphQL query execution
     * - Using variables with GraphQL
     * - Nested data querying
     * - Filter operations
     * 
     * @throws Exception If any error occurs during test execution
     */
    @Test
    public void testGraphQLPerformance() throws Exception {
        // Load the test configuration
        ConfigLoader configLoader = new ConfigLoader();
        TestConfig config = configLoader.loadConfig(CONFIG_FILE);

        // Log scenario information
        LOGGER.info("Running GraphQL performance test with {} scenario(s)",
                config.getScenarios() != null ? config.getScenarios().size() : 0);

        if (config.getScenarios() != null) {
            config.getScenarios().forEach(scenario -> {
                int totalRequests = 0;
                if (scenario.getRequests() != null) {
                    totalRequests += scenario.getRequests().size();
                }
                if (scenario.getGraphQLRequests() != null) {
                    totalRequests += scenario.getGraphQLRequests().size();
                }
                LOGGER.info("  - Scenario '{}' with {} request(s)", scenario.getName(), totalRequests);
            });
        }

        // Create a test executor
        TestExecutor executor = new TestExecutor();

        // Execute the test
        LOGGER.info("Executing GraphQL performance test...");
        boolean success = executor.execute(CONFIG_FILE);
        LOGGER.info("Test execution success: {}", success);

        // Log the summary - keep it simple to avoid API compatibility issues
        if (success) {
            LOGGER.info("Test completed successfully!");
            LOGGER.info("See HTML report for detailed statistics by sampler");
        } else {
            LOGGER.error("Test failed - see logs for details");
        }
    }

    /**
     * Main method to allow running from command line.
     * 
     * @param args Command line arguments (not used)
     * @throws Exception If any error occurs during execution
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting GraphQL performance test from main method");
        GraphQLTest test = new GraphQLTest();
        test.testGraphQLPerformance();
        LOGGER.info("GraphQL performance test completed");
        LOGGER.info("Reports are located in target/jmeter-reports directory");
    }
}
