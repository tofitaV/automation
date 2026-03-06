package com.automation.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class ConfigManager {
    private static final Properties PROPERTIES = new Properties();
    private static final String ENV_CONFIG_PATH = "config/env.properties";

    static {
        loadProperties(ENV_CONFIG_PATH);
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return PROPERTIES.getProperty(key);
    }

    public static String getRequired(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required config key is missing or blank: " + key);
        }
        return value;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private static void loadProperties(String classpathPath) {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (Objects.nonNull(input)) {
                PROPERTIES.load(input);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed loading config from: " + classpathPath, exception);
        }
    }
}
