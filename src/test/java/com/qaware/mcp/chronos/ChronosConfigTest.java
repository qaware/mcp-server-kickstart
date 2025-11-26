package com.qaware.mcp.chronos;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChronosConfigTest {

    @Test
    void testDefaultConfiguration() {
        ChronosConfig config = new ChronosConfig();

        assertThat(config.getBaseUrl()).isNotNull();
        assertThat(config.getTimeoutSeconds()).isPositive();
    }

    @Test
    void testBaseUrlFromProperties() {
        ChronosConfig config = new ChronosConfig();

        // Should load from application.properties
        assertThat(config.getBaseUrl()).isEqualTo("https://zeit-test.qaware.de");
    }

    @Test
    void testTimeoutFromProperties() {
        ChronosConfig config = new ChronosConfig();

        // Should load from application.properties
        assertThat(config.getTimeoutSeconds()).isEqualTo(20);
    }
}
