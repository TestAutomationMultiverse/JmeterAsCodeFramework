package com.perftest.utils;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON operations.
 */
public class JsonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Parses a JSON string into a map.
     *
     * @param json The JSON string
     * @return A map representation of the JSON
     * @throws IOException If the JSON cannot be parsed
     */
    public static Map<String, String> parseJsonToMap(String json) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            LOGGER.error("Failed to parse JSON to map: {}", json, e);
            throw e;
        }
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param object The object to convert
     * @return A JSON string representation of the object
     * @throws JsonProcessingException If the object cannot be converted to JSON
     */
    public static String toJson(Object object) throws JsonProcessingException {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to convert object to JSON", e);
            throw e;
        }
    }

    /**
     * Parses a JSON string into an object of the specified type.
     *
     * @param json      The JSON string
     * @param valueType The class of the object to parse into
     * @param <T>       The type of the object
     * @return An object of the specified type
     * @throws IOException If the JSON cannot be parsed
     */
    public static <T> T parseJson(String json, Class<T> valueType) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            LOGGER.error("Failed to parse JSON to {}: {}", valueType.getSimpleName(), json, e);
            throw e;
        }
    }

    /**
     * Checks if a string is valid JSON.
     *
     * @param json The string to check
     * @return True if the string is valid JSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
