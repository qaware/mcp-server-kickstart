package com.qaware.mcp.chronos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration loader for Chronos API settings.
 * Reads from application.properties and allows environment variables to override values.
 */
public class ChronosConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronosConfig.class);
    private static final String CONFIG_FILE = "application.properties";

    // Property keys
    private static final String BASE_URL_KEY = "chronos.api.base-url";
    private static final String TIMEOUT_KEY = "chronos.api.timeout-seconds";

    // Environment variable keys
    private static final String BASE_URL_ENV = "CHRONOS_API_BASE_URL";
    private static final String TIMEOUT_ENV = "CHRONOS_API_TIMEOUT_SECONDS";

    // Default values
    private static final String DEFAULT_BASE_URL = "https://zeit-test.qaware.de";
    private static final int DEFAULT_TIMEOUT_SECONDS = 20;

    private final String baseUrl;
    private final int timeoutSeconds;

    public ChronosConfig() {
        Properties properties = loadProperties();

        // Load base URL: environment variable takes precedence over properties file
        String envBaseUrl = System.getenv(BASE_URL_ENV);
        if (envBaseUrl != null && !envBaseUrl.isEmpty()) {
            this.baseUrl = envBaseUrl;
            LOGGER.info("Using Chronos base URL from environment variable: {}", baseUrl);
        } else {
            this.baseUrl = properties.getProperty(BASE_URL_KEY, DEFAULT_BASE_URL);
            LOGGER.info("Using Chronos base URL from config: {}", baseUrl);
        }

        // Load timeout: environment variable takes precedence over properties file
        int configuredTimeout = DEFAULT_TIMEOUT_SECONDS;
        String envTimeout = System.getenv(TIMEOUT_ENV);
        if (envTimeout != null && !envTimeout.isEmpty()) {
            try {
                configuredTimeout = Integer.parseInt(envTimeout);
                LOGGER.info("Using Chronos timeout from environment variable: {} seconds", configuredTimeout);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid timeout value in environment variable {}: '{}'. Using default: {}",
                        TIMEOUT_ENV, envTimeout, DEFAULT_TIMEOUT_SECONDS);
            }
        } else {
            String timeoutStr = properties.getProperty(TIMEOUT_KEY);
            if (timeoutStr != null) {
                try {
                    configuredTimeout = Integer.parseInt(timeoutStr);
                    LOGGER.info("Using Chronos timeout from config: {} seconds", configuredTimeout);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid timeout value in properties: '{}'. Using default: {}",
                            timeoutStr, DEFAULT_TIMEOUT_SECONDS);
                }
            } else {
                LOGGER.info("Using default Chronos timeout: {} seconds", configuredTimeout);
            }
        }
        this.timeoutSeconds = configuredTimeout;
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.warn("Unable to find {}. Using default configuration.", CONFIG_FILE);
                return properties;
            }
            properties.load(input);
            LOGGER.debug("Loaded configuration from {}", CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.warn("Error loading configuration from {}: {}. Using defaults.",
                    CONFIG_FILE, e.getMessage());
        }
        return properties;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
