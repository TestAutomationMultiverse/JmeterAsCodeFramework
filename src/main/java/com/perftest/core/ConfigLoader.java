package com.perftest.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.perftest.config.TestConfig;

/**
 * Utility class for loading YAML test configuration files.
 */
public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    /**
     * Loads a test configuration from a YAML file.
     *
     * @param configPath The path to the YAML configuration file
     * @return The parsed TestConfig object
     * @throws IOException If the file cannot be read or is not valid YAML
     */
    public TestConfig loadConfig(String configPath) throws IOException {
        LOGGER.info("Loading test configuration from {}", configPath);

        // Try various paths with the new folder structure
        Path path = findConfigFile(configPath);
        if (path == null) {
            throw new IOException("Configuration file not found: " + configPath);
        }

        LOGGER.info("Found configuration file at: {}", path);

        try (InputStream input = new FileInputStream(path.toFile())) {
            Yaml yaml = new Yaml(new Constructor(TestConfig.class));
            TestConfig config = yaml.load(input);

            LOGGER.info("Successfully loaded test configuration with {} scenario(s)",
                    config.getScenarios() != null ? config.getScenarios().size() : 0);

            validateConfig(config);
            return config;
        } catch (Exception e) {
            LOGGER.error("Failed to load test configuration", e);
            throw new IOException("Failed to load test configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Finds the configuration file in various possible locations.
     * 
     * @param configPath The original configuration path
     * @return The Path to the configuration file, or null if not found
     */
    private Path findConfigFile(String configPath) {
        // First check the original path
        Path originalPath = Paths.get(configPath);
        if (Files.exists(originalPath)) {
            return originalPath;
        }

        // Try in configs subfolder
        Path configsPath = Paths.get("src/test/resources/configs", Paths.get(configPath).getFileName().toString());
        if (Files.exists(configsPath)) {
            return configsPath;
        }

        // Try relative to resources directory
        Path resourcePath = Paths.get("src/test/resources", Paths.get(configPath).getFileName().toString());
        if (Files.exists(resourcePath)) {
            return resourcePath;
        }

        // Try just the filename in various locations
        String fileName = Paths.get(configPath).getFileName().toString();

        // Configs folder
        Path inConfigsFolder = Paths.get("src/test/resources/configs", fileName);
        if (Files.exists(inConfigsFolder)) {
            return inConfigsFolder;
        }

        // Root resources folder
        Path inRootFolder = Paths.get("src/test/resources", fileName);
        if (Files.exists(inRootFolder)) {
            return inRootFolder;
        }

        LOGGER.warn("Could not find configuration file in any location: {}", configPath);
        return null;
    }

    /**
     * Validates that the loaded configuration contains the required elements.
     *
     * @param config The configuration to validate
     * @throws IllegalArgumentException If the configuration is invalid
     */
    private void validateConfig(TestConfig config) {
        if (config.getScenarios() == null || config.getScenarios().isEmpty()) {
            throw new IllegalArgumentException("Test configuration must contain at least one scenario");
        }

        if (config.getExecution() == null) {
            throw new IllegalArgumentException("Test configuration must contain execution parameters");
        }

        LOGGER.info("Test configuration validation passed");
    }
}
