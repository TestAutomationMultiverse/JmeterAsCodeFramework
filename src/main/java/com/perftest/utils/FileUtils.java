package com.perftest.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for file operations.
 */
public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Reads a file as a string.
     *
     * @param filePath The path to the file
     * @return The content of the file as a string
     * @throws IOException If the file cannot be read
     */
    public static String readFileAsString(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            LOGGER.error("File not found: {}", filePath);
            throw new IOException("File not found: " + filePath);
        }

        return new String(Files.readAllBytes(path));
    }

    /**
     * Checks if a file exists.
     *
     * @param filePath The path to the file
     * @return True if the file exists, false otherwise
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Lists all files in a directory.
     *
     * @param directoryPath The path to the directory
     * @return A list of files in the directory
     * @throws IOException If the directory cannot be read
     */
    public static List<String> listFiles(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            LOGGER.error("Directory not found: {}", directoryPath);
            throw new IOException("Directory not found: " + directoryPath);
        }

        try (Stream<Path> paths = Files.list(path)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Creates a directory if it doesn't exist.
     *
     * @param directoryPath The path to the directory
     * @throws IOException If the directory cannot be created
     */
    public static void createDirectoryIfNotExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);

        if (!Files.exists(path)) {
            LOGGER.info("Creating directory: {}", directoryPath);
            Files.createDirectories(path);
        }
    }

    /**
     * Writes content to a file.
     *
     * @param filePath The path to the file
     * @param content  The content to write
     * @throws IOException If the file cannot be written
     */
    public static void writeToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);

        // Create parent directories if they don't exist
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Files.write(path, content.getBytes());
    }
}
