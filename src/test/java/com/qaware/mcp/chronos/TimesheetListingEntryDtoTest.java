package com.qaware.mcp.chronos;

import de.qaware.qaerp.chronos.client.model.Status;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimesheetListingEntryDtoTest {

    @Test
    void testRecordCreation() {
        TimesheetListingEntryDto dto = new TimesheetListingEntryDto(
                "12345",
                Status.DRAFT,
                "2024-11"
        );

        assertThat(dto.contract()).isEqualTo("12345");
        assertThat(dto.status()).isEqualTo(Status.DRAFT);
        assertThat(dto.yearMonth()).isEqualTo("2024-11");
    }

    @Test
    void testRecordWithDifferentStatus() {
        TimesheetListingEntryDto dto = new TimesheetListingEntryDto(
                "67890",
                Status.SUBMITTED,
                "2023-05"
        );

        assertThat(dto.contract()).isEqualTo("67890");
        assertThat(dto.status()).isEqualTo(Status.SUBMITTED);
        assertThat(dto.yearMonth()).isEqualTo("2023-05");
    }

    @Test
    void testRecordWithApprovedStatus() {
        TimesheetListingEntryDto dto = new TimesheetListingEntryDto(
                "11111",
                Status.APPROVED,
                "2024-12"
        );

        assertThat(dto.contract()).isEqualTo("11111");
        assertThat(dto.status()).isEqualTo(Status.APPROVED);
        assertThat(dto.yearMonth()).isEqualTo("2024-12");
    }

    @Test
    void testRecordEquality() {
        TimesheetListingEntryDto dto1 = new TimesheetListingEntryDto("12345", Status.DRAFT, "2024-11");
        TimesheetListingEntryDto dto2 = new TimesheetListingEntryDto("12345", Status.DRAFT, "2024-11");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void testRecordInequality() {
        TimesheetListingEntryDto dto1 = new TimesheetListingEntryDto("12345", Status.DRAFT, "2024-11");
        TimesheetListingEntryDto dto2 = new TimesheetListingEntryDto("67890", Status.SUBMITTED, "2024-12");

        assertThat(dto1).isNotEqualTo(dto2);
    }
}
