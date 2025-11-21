package com.qaware.mcp.tools;

import de.qaware.qaerp.chronos.client.model.Status;
import de.qaware.qaerp.chronos.client.model.TimesheetListingEntry;

public record TimesheetListingEntryDto(
        String contract,
    Status status,
    String yearMonth) {

    public static TimesheetListingEntryDto of(TimesheetListingEntry clientEntry) {
        return new TimesheetListingEntryDto(
                clientEntry.getContract(),
                clientEntry.getStatus(),
                clientEntry.getYearMonth().toString()
        );
    }
}
