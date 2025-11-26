package com.qaware.mcp.chronos;

import de.qaware.qaerp.chronos.client.model.Status;
import de.qaware.qaerp.chronos.client.model.TimesheetListingEntry;

/**
 * Data transfer object for timesheet listing entries.
 * Represents a summary of a timesheet for a specific contract and month.
 *
 * @param contract The contract number for this timesheet
 * @param status The current status of the timesheet (e.g., DRAFT, SUBMITTED, APPROVED)
 * @param yearMonth The year and month in string format (e.g., "2024-11")
 */
public record TimesheetListingEntryDto(
        String contract,
    Status status,
    String yearMonth) {

    /**
     * Converts a TimesheetListingEntry from the Chronos client to a DTO.
     *
     * @param clientEntry The client model entry to convert
     * @return A new TimesheetListingEntryDto with the same data
     */
    public static TimesheetListingEntryDto of(TimesheetListingEntry clientEntry) {
        return new TimesheetListingEntryDto(
                clientEntry.getContract(),
                clientEntry.getStatus(),
                clientEntry.getYearMonth().toString()
        );
    }
}
