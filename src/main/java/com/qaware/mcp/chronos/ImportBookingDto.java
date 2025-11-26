package com.qaware.mcp.chronos;

/**
 * Data transfer object for import bookings.
 * Represents a single booking entry to be imported into Chronos.
 *
 * @param projectName Name of the project to book time to
 * @param accountName Name of the account within the project
 * @param comment Optional comment describing the work performed
 * @param duration Duration of the booking in ISO-8601 format (e.g., "PT2H30M" for 2 hours 30 minutes)
 */
public record ImportBookingDto(String projectName, String accountName, String comment, String duration) {
}
