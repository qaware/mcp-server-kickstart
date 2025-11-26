package com.qaware.mcp.chronos;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChronosMCPExceptionTest {

    @Test
    void testMessageOnlyConstructor() {
        String message = "Test error message";
        ChronosMCPException exception = new ChronosMCPException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testMessageAndCauseConstructor() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        ChronosMCPException exception = new ChronosMCPException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testCauseOnlyConstructor() {
        Throwable cause = new RuntimeException("Root cause");
        ChronosMCPException exception = new ChronosMCPException(cause);

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains("RuntimeException");
    }

    @Test
    void testExceptionIsRuntimeException() {
        ChronosMCPException exception = new ChronosMCPException("Test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
