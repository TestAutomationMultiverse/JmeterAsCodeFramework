package com.perftest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * A utility class to help reorganize resource files from flat structure to subfolder structure.
 * This is used during the testing and development process and can be removed in the final version.
 */
public class ResourceReorganizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceReorganizer.class);
    private static final String RESOURCES_BASE_PATH = "src/test/resources/";

    /**
     * Organize resources from the root folder into respective subfolders.
     * This method will copy (not move) files to maintain backward compatibility.
     */
    public static void organizeResources() {
        LOGGER.info("Starting resource reorganization");
        
        try {
            // Ensure subfolders exist
            createSubfolders();
            
            // Copy JSON files to their respective folders
            Path resourcesPath = Paths.get(RESOURCES_BASE_PATH);
            if (Files.exists(resourcesPath) && Files.isDirectory(resourcesPath)) {
                Files.list(resourcesPath).forEach(file -> {
                    String fileName = file.getFileName().toString();
                    
                    if (Files.isRegularFile(file)) {
                        try {
                            if (fileName.endsWith("_body.json")) {
                                copyToSubfolder(file, "body");
                            } else if (fileName.endsWith("_headers.json")) {
                                copyToSubfolder(file, "headers");
                            } else if (fileName.endsWith(".schema.json")) {
                                copyToSubfolder(file, "schemas");
                            } else if (fileName.endsWith("_params.template")) {
                                copyToSubfolder(file, "params");
                            } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                                copyToSubfolder(file, "configs");
                            } else if (fileName.endsWith(".csv") || fileName.endsWith(".dat")) {
                                copyToSubfolder(file, "data");
                            }
                        } catch (IOException e) {
                            LOGGER.error("Failed to copy file: {}", fileName, e);
                        }
                    }
                });
            }
            
            LOGGER.info("Resource reorganization completed");
        } catch (IOException e) {
            LOGGER.error("Failed to organize resources", e);
        }
    }
    
    /**
     * Create the subfolder structure if it doesn't exist.
     */
    private static void createSubfolders() throws IOException {
        String[] subfolders = {"body", "headers", "params", "schemas", "configs", "data"};
        
        for (String subfolder : subfolders) {
            Path folderPath = Paths.get(RESOURCES_BASE_PATH, subfolder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
                LOGGER.info("Created subfolder: {}", folderPath);
            }
        }
    }
    
    /**
     * Copy a file to the specified subfolder.
     */
    private static void copyToSubfolder(Path sourceFile, String subfolder) throws IOException {
        Path targetPath = Paths.get(RESOURCES_BASE_PATH, subfolder, sourceFile.getFileName().toString());
        Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Copied {} to {}", sourceFile.getFileName(), targetPath);
    }
}