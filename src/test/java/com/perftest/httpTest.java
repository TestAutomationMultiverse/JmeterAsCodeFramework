package com.perftest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.perftest.config.TestConfig;
import com.perftest.core.ConfigLoader;
import com.perftest.core.TestExecutor;

public class httpTest {

    /**
     * Parses a YAML string into a generic Map structure
     * Used by other test classes for simplified YAML parsing
     * 
     * @param yamlContent The YAML content as a string
     * @return A Map containing the parsed YAML structure
     * @throws IOException If there's an error parsing the YAML
     */
    public static Map<String, Object> parseYaml(String yamlContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yamlContent, Map.class);
    }

    @Test
    public void testWithYamlConfig() throws Exception {
        // Get the path to the YAML configuration file
        String configFilePath = Paths.get("src", "test", "resources", "configs", "http_test_config.yaml").toString();
        File configFile = new File(configFilePath);

        // Make sure the config file exists
        if (!configFile.exists()) {
            throw new RuntimeException("Config file not found: " + configFilePath);
        }

        // Load the configuration from the YAML file
        ConfigLoader configLoader = new ConfigLoader();
        TestConfig config = configLoader.loadConfig(configFilePath);

        // Print some information about the loaded config
        System.out.println("Loaded test configuration:");
        System.out.println("- Number of scenarios: " + config.getScenarios().size());
        System.out.println("- Thread count: " + config.getExecution().getThreads());
        System.out.println("- Iteration count: " + config.getExecution().getIterations());

        // Execute the tests
        TestExecutor executor = new TestExecutor();
        boolean success = executor.execute(configFilePath);
        System.out.println("Test execution success: " + success);

        // If no exception is thrown, the test is considered successful
    }
}
