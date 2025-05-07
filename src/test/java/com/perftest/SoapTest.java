package com.perftest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perftest.config.TestConfig;
import com.perftest.core.ConfigLoader;
import com.perftest.core.TestExecutor;

/**
 * Test class for running SOAP performance tests against the Beeceptor mock SOAP
 * service.
 * <p>
 * This test demonstrates how to use the JMeter DSL Framework to test SOAP APIs
 * by sending
 * SOAP envelopes with various payloads, including:
 * <ul>
 * <li>Weather information requests</li>
 * <li>Flight status information requests</li>
 * </ul>
 * <p>
 * The Beeceptor mock SOAP service
 * (https://app.beeceptor.com/mock-server/soap-service-free) is used
 * as the test endpoint since it's publicly available for testing SOAP services.
 */
public class SoapTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoapTest.class);
    private static final String CONFIG_FILE = "src/test/resources/configs/soap_test_config.yaml";

    @BeforeAll
    public static void setup() {
        // This method can be used for test-wide setup if needed
        LOGGER.info("Setting up SOAP performance test");
    }

    /**
     * Executes a performance test against the Beeceptor mock SOAP service.
     * <p>
     * This test loads configuration from the YAML file, which defines multiple
     * SOAP requests including different operations and XML validation.
     * <p>
     * The test demonstrates:
     * - Basic SOAP request execution
     * - Using variables within SOAP envelopes
     * - XPath response validation
     * - Handling of SOAP headers
     * 
     * @throws Exception If any error occurs during test execution
     */
    @Test
    public void testSoapPerformance() throws Exception {
        // Load the test configuration
        ConfigLoader configLoader = new ConfigLoader();
        TestConfig config = configLoader.loadConfig(CONFIG_FILE);

        // Log scenario information
        LOGGER.info("Running SOAP performance test with {} scenario(s)",
                config.getScenarios() != null ? config.getScenarios().size() : 0);

        if (config.getScenarios() != null) {
            config.getScenarios().forEach(scenario -> {
                int totalRequests = 0;
                if (scenario.getRequests() != null) {
                    totalRequests += scenario.getRequests().size();
                }
                if (scenario.getSoapRequests() != null) {
                    totalRequests += scenario.getSoapRequests().size();
                }
                LOGGER.info("  - Scenario '{}' with {} request(s)", scenario.getName(), totalRequests);
            });
        }

        // Create a test executor
        TestExecutor executor = new TestExecutor();

        // Execute the test
        LOGGER.info("Executing SOAP performance test...");
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
}