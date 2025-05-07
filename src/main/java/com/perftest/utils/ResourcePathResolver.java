package com.perftest.utils;

/**
 * Utility class for resolving resource paths with the new folder structure.
 * This provides a convenient way to refer to resources in the new organized
 * structure.
 */
public class ResourcePathResolver {
    // private static final Logger LOGGER =
    // LoggerFactory.getLogger(ResourcePathResolver.class);
    private static final String BODY_FOLDER = "body/";
    private static final String HEADERS_FOLDER = "headers/";
    private static final String PARAMS_FOLDER = "params/";
    private static final String SCHEMAS_FOLDER = "schemas/";
    private static final String CONFIGS_FOLDER = "configs/";
    private static final String DATA_FOLDER = "data/";

    /**
     * Resolves a body resource path.
     * 
     * @param filename The filename
     * @return The path with the correct subfolder
     */
    public static String body(String filename) {
        return BODY_FOLDER + filename;
    }

    /**
     * Resolves a headers resource path.
     * 
     * @param filename The filename
     * @return The path with the correct subfolder
     */
    public static String headers(String filename) {
        return HEADERS_FOLDER + filename;
    }

    /**
     * Resolves a params resource path.
     * 
     * @param filename The filename
     * @return The path with the correct subfolder
     */
    public static String params(String filename) {
        return PARAMS_FOLDER + filename;
    }

    /**
     * Resolves a schema resource path.
     * 
     * @param filename The filename
     * @return The path with the correct subfolder
     */
    public static String schema(String filename) {
        return SCHEMAS_FOLDER + filename;
    }

    /**
     * Resolves a config resource path.
     * 
     * @param filename The filename
     * @return The path with the correct subfolder
     */
    public static String config(String filename) {
        return CONFIGS_FOLDER + filename;
    }

    /**
     * Resolves a data resource path.
     * 
     * @param filename The filename
     * @return The path with the correct subfolder
     */
    public static String data(String filename) {
        return DATA_FOLDER + filename;
    }
}