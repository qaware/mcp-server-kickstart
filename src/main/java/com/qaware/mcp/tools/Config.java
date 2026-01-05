package com.qaware.mcp.tools;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal configuration helper for MCP tools.
 *
 * <p>Loads key/value pairs from environment variables and Java system properties into an internal
 * cache. Keys are normalized (lowercase; remove whitespace, "_", "-" and ".") so that e.g.
 * {@code MAX_CONTENT}, {@code max-content} and {@code max.content} are equivalent.
 *
 * <p>This is intentionally not a full configuration framework.
 */
public enum Config {

    ;


    public static final String MAX_CONTENT = "mcp-kb-max-content";
    public static final String ROOT = "mcp-kb-root";

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    @SuppressWarnings("java:S1700")
    private static final Map<String, String> config = new LinkedHashMap<>();


    static {
        reset();
    }


    static void reset() {
        config.clear();

        System.getenv().forEach(Config::putNormalized);
        System.getProperties().forEach(Config::putNormalized);
    }


    /** Returns the configured value for the given key (after normalization) or {@code null}. */
    public static String get(String key) {
        LOGGER.debug("Getting key {} from configuration", key);

        return config.get(normalizeKey(key));
    }


    /** Returns the configured value for the given key (after normalization) or {@code fallback} if the key is not set. */
    public static String get(String key, String fallback) {
        String value = get(key);
        return value == null ? fallback : value;
    }


    /** Returns the configured int value for the given key (after normalization) or {@code fallback} if the key is not set. */
    public static int getInt(String key, int fallback) {
        String value = get(key);
        return value == null ? fallback : Integer.parseInt(value);
    }


    private static String normalizeKey(Object key) {
        if (key == null) {
            return null;
        }
        return key.toString().toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "").replace(".", "").trim();
    }


    private static void putNormalized(Object key, Object value) {
        if (value != null) {
            config.put(normalizeKey(key), value.toString());
        }
    }

}
