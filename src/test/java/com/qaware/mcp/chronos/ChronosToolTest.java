package com.qaware.mcp.chronos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ChronosTool focusing on validation logic and exception handling.
 * Note: Tests that require mocking Chronos client classes have been omitted due to Java 25 module restrictions.
 * Integration tests with real Chronos client instances should be created separately.
 */
class ChronosToolTest {

    @Test
    void testDefaultConstructor() {
        ChronosTool tool = new ChronosTool();
        assertThat(tool).isNotNull();
    }

    @Test
    void testConstructorWithFactory() {
        ChronosClientFactory factory = new ChronosClientFactory();
        ChronosTool tool = new ChronosTool(factory);
        assertThat(tool).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testBookWorkingDayChronosMCP_InvalidContract(String contract) {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                contract,
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Contract number is required");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testBookWorkingDayChronosMCP_InvalidToken(String token) {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                token
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Authorization token is required");
    }

    @Test
    void testBookWorkingDayChronosMCP_NullBookings() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                null,
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("At least one booking is required");
    }

    @Test
    void testBookWorkingDayChronosMCP_EmptyBookings() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                Collections.emptyList(),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("At least one booking is required");
    }

    @Test
    void testBookWorkingDayChronosMCP_InvalidDateFormat() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "invalid-date",
                "09:00",
                "17:00",
                "PT1H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Invalid date/time format provided");
    }

    @Test
    void testBookWorkingDayChronosMCP_InvalidTimeFormat() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "invalid-time",
                "17:00",
                "PT1H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Invalid date/time format provided");
    }

    @Test
    void testBookWorkingDayChronosMCP_InvalidDurationFormat() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "invalid-duration",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Invalid date/time format provided");
    }

    @Test
    void testBookWorkingDayChronosMCP_StartTimeAfterEndTime() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "17:00",
                "09:00",
                "PT1H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("must be before end time");
    }

    @Test
    void testBookWorkingDayChronosMCP_NegativeBreakDuration() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT-1H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Break duration cannot be negative");
    }

    @Test
    void testBookWorkingDayChronosMCP_BreakDurationExceedsTotalTime() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT9H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("cannot be >= total work time");
    }

    @Test
    void testBookWorkingDayChronosMCP_BreakDurationEqualsTotalTime() {
        ChronosTool tool = new ChronosTool();

        assertThatThrownBy(() -> tool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT8H",
                Collections.singletonList("{\"projectName\":\"Test\",\"accountName\":\"Dev\",\"comment\":\"\",\"duration\":\"PT7H\"}"),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("cannot be >= total work time");
    }
}
