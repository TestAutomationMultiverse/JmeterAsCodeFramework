package com.perftest.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading test resource files.
 */
public class ResourceLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);
    private static final String RESOURCES_BASE_PATH = "src/test/resources/";

    /**
     * Loads a resource file as a string.
     *
     * @param resourceName The name of the resource file
     * @return The content of the resource file as a string
     * @throws IOException If the file cannot be read
     */
    public String loadResource(String resourceName) throws IOException {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            LOGGER.error("Resource name is null or empty");
            throw new IllegalArgumentException("Resource name cannot be null or empty");
        }

        // Check if it's a template file by its extension or path
        boolean isTemplate = resourceName.contains("template") ||
                resourceName.endsWith(".xml") ||
                resourceName.endsWith(".graphql") ||
                resourceName.endsWith(".gql");

        Path resourcePath = getResourcePath(resourceName);

        if (!Files.exists(resourcePath)) {
            String message = "Resource file not found: " + resourcePath;
            if (isTemplate) {
                message = "Template file not found: " + resourcePath +
                        ". Please ensure the template exists in the appropriate templates directory.";
            }
            LOGGER.error(message);
            throw new IOException(message);
        }

        LOGGER.debug("Loading resource: {}", resourcePath);
        try {
            return new String(Files.readAllBytes(resourcePath));
        } catch (IOException e) {
            LOGGER.error("Failed to read resource file: {}", resourcePath, e);
            throw new IOException("Failed to read resource file: " + resourcePath, e);
        }
    }

    /**
     * Gets the full path for a CSV data file.
     *
     * @param dataFileName The name of the CSV data file
     * @return The full path to the CSV data file
     */
    public String loadCsvDataFilePath(String dataFileName) {
        Path dataFilePath = getResourcePath(dataFileName);

        if (!Files.exists(dataFilePath)) {
            LOGGER.warn("CSV data file not found: {}", dataFilePath);
        }

        return dataFilePath.toString();
    }

    /**
     * Loads a template file specifically.
     * This method provides specialized error handling for template files.
     *
     * @param templatePath The path to the template file
     * @param templateType The type of template (e.g., "SOAP", "GraphQL")
     * @return The content of the template file as a string
     * @throws IOException If the template file cannot be read
     */
    public String loadTemplate(String templatePath, String templateType) throws IOException {
        if (templatePath == null || templatePath.trim().isEmpty()) {
            LOGGER.error("{} template path is null or empty", templateType);
            throw new IllegalArgumentException(templateType + " template path cannot be null or empty");
        }

        // Try to load the resource
        try {
            String content = loadResource(templatePath);
            LOGGER.info("Successfully loaded {} template from: {}", templateType, templatePath);
            return content;
        } catch (IOException e) {
            LOGGER.error("Failed to load {} template from: {}", templateType, templatePath, e);
            throw new IOException("Failed to load " + templateType + " template: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the absolute path for a resource file, checking in the new subfolder
     * structure.
     *
     * @param resourceName The name of the resource file
     * @return The absolute path to the resource file
     */
    private Path getResourcePath(String resourceName) {
        LOGGER.debug("Searching for resource: {}", resourceName);

        // First, see if the path as provided exists (could be absolute or relative)
        Path directPath = Paths.get(resourceName);
        if (Files.exists(directPath)) {
            LOGGER.debug("Found resource at direct path: {}", directPath);
            return directPath;
        }

        // Next, try with target/test-classes prefix since that's where Maven puts test
        // resources
        Path targetClassesPath = Paths.get("target/test-classes", resourceName);
        if (Files.exists(targetClassesPath)) {
            LOGGER.debug("Found resource in target/test-classes: {}", targetClassesPath);
            return targetClassesPath;
        }

        // Try with src/test/resources prefix
        Path srcResourcesPath = Paths.get(RESOURCES_BASE_PATH, resourceName);
        if (Files.exists(srcResourcesPath)) {
            LOGGER.debug("Found resource in src/test/resources: {}", srcResourcesPath);
            return srcResourcesPath;
        }

        // Define potential folder locations based on resource type
        String[] targetFolders = {
                "", // Root resources folder
                "body/", // Request bodies
                "headers/", // Request headers
                "params/", // Request parameters
                "schemas/", // Response schemas
                "configs/", // Configuration files
                "data/", // Test data files
                "templates/", // Template directory
                "templates/soap/", // SOAP templates
                "templates/graphql/" // GraphQL templates
        };

        // Check each possible location in target/test-classes (compiled resources)
        for (String folder : targetFolders) {
            Path tryPath = Paths.get("target/test-classes", folder, resourceName);
            if (Files.exists(tryPath)) {
                LOGGER.debug("Found resource in target/test-classes/{}: {}", folder, tryPath);
                return tryPath;
            }
        }

        // Check each possible location in src/test/resources
        for (String folder : targetFolders) {
            Path tryPath = Paths.get(RESOURCES_BASE_PATH, folder, resourceName);
            if (Files.exists(tryPath)) {
                LOGGER.debug("Found resource in src/test/resources/{}: {}", folder, tryPath);
                return tryPath;
            }
        }

        // As a last resort, try classpath (though we generally avoid this due to
        // potential path issues)
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resource = classLoader.getResource(resourceName);

            if (resource != null) {
                Path classpathPath = Paths.get(resource.toURI());
                LOGGER.debug("Found resource in classpath: {}", classpathPath);
                return classpathPath;
            }
        } catch (Exception e) {
            LOGGER.warn("Error trying to load from classpath: {}", e.getMessage());
        }

        // Still not found, fall back to original path in resources directory
        // (this will likely fail with IOException when attempted to be read)
        LOGGER.warn("Resource not found in any location, returning default path: {}",
                Paths.get(RESOURCES_BASE_PATH, resourceName));
        return Paths.get(RESOURCES_BASE_PATH, resourceName);
    }
}
