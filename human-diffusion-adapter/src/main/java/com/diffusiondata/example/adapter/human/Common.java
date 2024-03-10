package com.diffusiondata.example.adapter.human;

import static java.lang.String.format;

import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class Common {

    /**
     * Fetch a text resource relative to a given class for initialising a constant.
     * @param clazz Fetch resource relative to this class
     * @param resourceName Name of resource
     * @return The file content
     * @throws ExceptionInInitializerError if the resource cannot be found
     */
    public static String getResourceFileAsString(Class<?> clazz, String resourceName) {
        try (InputStream is = clazz.getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Unknown resource " + resourceName);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
        catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }


    /**
     * Get a named parameter, applying a default if absent, checking for appropriate type.
     * @param map Parameter store
     * @param key Parameter name
     * @param expectedType Class matching T
     * @param defaultValue the default value
     * @return The configuration item
     * @param <T> Expected type of the configuration item
     * @throws InvalidConfigurationException if the configuration item is of an incorrect type.
     */
    public static <T> T getParam(
        Map<String, Object> map,
        String key,
        Class<T> expectedType,
        T defaultValue
    ) throws InvalidConfigurationException {
        final Object result = map.get(key);
        if (result == null) {
            return defaultValue;
        }
        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        }
        throw new InvalidConfigurationException(
            format("Configuration parameter `%s` must be a %s", key, defaultValue.getClass().getName()));
    }
}
