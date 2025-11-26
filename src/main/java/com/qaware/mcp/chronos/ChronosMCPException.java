package com.qaware.mcp.chronos;

/**
 * Exception thrown when an error occurs during MCP tool operations with Chronos.
 * This is a runtime exception that wraps underlying errors and provides context
 * about what operation failed.
 */
public class ChronosMCPException extends RuntimeException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message The detail message explaining the error
     */
    public ChronosMCPException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message The detail message explaining the error
     * @param cause The underlying cause of the error
     */
    public ChronosMCPException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with the specified cause.
     *
     * @param cause The underlying cause of the error
     */
    public ChronosMCPException(Throwable cause) {
        super(cause);
    }
}
