package com.qaware.mcp.chronos;

import de.qaware.qaerp.chronos.client.api.ProjectsClient;
import de.qaware.qaerp.chronos.client.api.TimesheetsClient;
import de.qaware.qaerp.chronos.client.api.error.ChronosClientException;
import de.qaware.qaerp.chronos.client.model.*;
import io.vavr.control.Either;
import org.junit.jupiter.api.*;

import java.time.LocalTime;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for ChronosTool that verify the complete flow of operations
 * with fake Chronos API clients.
 *
 * <p>These tests verify the integration between ChronosTool and the Chronos client layer,
 * testing error handling and business logic flow without making real API calls.
 *
 * <p>Note: Due to Java 25 module restrictions with Mockito, we use manual test doubles
 * (fake implementations) instead of mocking frameworks.
 */
class ChronosToolIntegrationTest {

    private FakeProjectsClient projectsClient;
    private FakeTimesheetsClient timesheetsClient;
    private ChronosTool chronosTool;

    @BeforeEach
    void setUp() {
        projectsClient = new FakeProjectsClient();
        timesheetsClient = new FakeTimesheetsClient();
        chronosTool = new ChronosTool(projectsClient, timesheetsClient);
    }

    // ===== listProjectsChronosMCP Tests =====

    @Test
    @DisplayName("Integration: List projects delegates to ProjectsClient")
    void testListProjectsChronosMCP_DelegatesToClient() {
        YearMonth yearMonth = YearMonth.of(2024, 11);
        projectsClient.setListProjectsResult(Collections.emptyList());

        List<ProjectListingEntry> result = chronosTool.listProjectsChronosMCP(2024, 11, "valid-token");

        assertThat(result).isNotNull().isEmpty();
        assertThat(projectsClient.listProjectsCallCount).isEqualTo(1);
        assertThat(projectsClient.lastYearMonth).isEqualTo(yearMonth);
        assertThat(projectsClient.lastToken).isEqualTo("valid-token");
    }

    @Test
    @DisplayName("Integration: List projects with null parameters uses current month")
    void testListProjectsChronosMCP_NullParametersDefaultsToCurrent() {
        projectsClient.setListProjectsResult(Collections.emptyList());

        List<ProjectListingEntry> result = chronosTool.listProjectsChronosMCP(null, null, "valid-token");

        assertThat(result).isNotNull().isEmpty();
        assertThat(projectsClient.listProjectsCallCount).isEqualTo(1);
        assertThat(projectsClient.lastYearMonth).isNotNull();
        assertThat(projectsClient.lastToken).isEqualTo("valid-token");
    }

    @Test
    @DisplayName("Integration: List projects handles InterruptedException")
    void testListProjectsChronosMCP_HandlesInterruptedException() {
        projectsClient.setListProjectsException(new InterruptedException("Connection interrupted"));

        assertThatThrownBy(() -> chronosTool.listProjectsChronosMCP(2024, 11, "valid-token"))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Failed to fetch projects")
                .hasMessageContaining("2024-11")
                .hasCauseInstanceOf(InterruptedException.class);

        assertThat(Thread.interrupted()).describedAs("Thread interrupt flag should be restored").isTrue();
    }

    // ===== retrieveUserContextChronosMCP Tests =====

    @Test
    @DisplayName("Integration: Retrieve user context handles empty timesheets")
    void testRetrieveUserContextChronosMCP_EmptyTimesheets() {
        timesheetsClient.setListResult(Collections.emptyList());

        UserContext result = chronosTool.retrieveUserContextChronosMCP("token");

        assertThat(result.activeContracts()).isEmpty();
        assertThat(result.recentlyBookedProjects()).isEmpty();
        assertThat(result.recentlyBookedAccounts()).isEmpty();
        assertThat(timesheetsClient.listCallCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Integration: Retrieve user context handles ChronosClientException gracefully")
    void testRetrieveUserContextChronosMCP_HandlesClientException() {
        timesheetsClient.setListResult(Collections.emptyList());
        timesheetsClient.setExportException(new ChronosClientException("No data available"));

        UserContext result = chronosTool.retrieveUserContextChronosMCP("token");

        assertThat(result.activeContracts()).isEmpty();
        assertThat(result.recentlyBookedProjects()).isEmpty();
        assertThat(result.recentlyBookedAccounts()).isEmpty();
    }

    @Test
    @DisplayName("Integration: Retrieve user context handles InterruptedException")
    void testRetrieveUserContextChronosMCP_HandlesInterruptedException() {
        timesheetsClient.setListException(new InterruptedException("Connection interrupted"));

        assertThatThrownBy(() -> chronosTool.retrieveUserContextChronosMCP("token"))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Failed to retrieve user context")
                .hasCauseInstanceOf(InterruptedException.class);

        assertThat(Thread.interrupted()).describedAs("Thread interrupt flag should be restored").isTrue();
    }

    // ===== getTimesheetsChronosMCP Tests =====

    @Test
    @DisplayName("Integration: Get timesheets delegates to TimesheetsClient")
    void testGetTimesheetsChronosMCP_DelegatesToClient() {
        YearMonth yearMonth = YearMonth.of(2024, 11);
        timesheetsClient.setListResult(Collections.emptyList());

        List<TimesheetListingEntryDto> result = chronosTool.getTimesheetsChronosMCP(2024, 11, "token");

        assertThat(result).isNotNull().isEmpty();
        assertThat(timesheetsClient.listCallCount).isEqualTo(1);
        assertThat(timesheetsClient.lastYearMonth).isEqualTo(yearMonth);
        assertThat(timesheetsClient.lastToken).isEqualTo("token");
    }

    @Test
    @DisplayName("Integration: Get timesheets with null parameters uses current month")
    void testGetTimesheetsChronosMCP_NullParametersDefaultsToCurrent() {
        timesheetsClient.setListResult(Collections.emptyList());

        List<TimesheetListingEntryDto> result = chronosTool.getTimesheetsChronosMCP(null, null, "token");

        assertThat(result).isNotNull().isEmpty();
        assertThat(timesheetsClient.listCallCount).isEqualTo(1);
        assertThat(timesheetsClient.lastYearMonth).isNotNull();
        assertThat(timesheetsClient.lastToken).isEqualTo("token");
    }

    @Test
    @DisplayName("Integration: Get timesheets handles InterruptedException")
    void testGetTimesheetsChronosMCP_HandlesInterruptedException() {
        timesheetsClient.setListException(new InterruptedException("Connection interrupted"));

        assertThatThrownBy(() -> chronosTool.getTimesheetsChronosMCP(2024, 11, "token"))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Failed to fetch timesheets")
                .hasMessageContaining("2024-11")
                .hasCauseInstanceOf(InterruptedException.class);

        assertThat(Thread.interrupted()).describedAs("Thread interrupt flag should be restored").isTrue();
    }

    // ===== bookWorkingDayChronosMCP Tests =====

    @Test
    @DisplayName("Integration: Book working day handles InterruptedException during export")
    void testBookWorkingDayChronosMCP_HandlesInterruptedExceptionOnExport() {
        timesheetsClient.setExportException(new InterruptedException("Connection interrupted"));

        String bookingJson = "{\"projectName\":\"Project\",\"accountName\":\"Account\",\"comment\":\"Work\",\"duration\":\"PT7H\"}";

        assertThatThrownBy(() -> chronosTool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                Collections.singletonList(bookingJson),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Booking operation was interrupted")
                .hasMessageContaining("2024-11-26")
                .hasCauseInstanceOf(InterruptedException.class);

        assertThat(Thread.interrupted()).describedAs("Thread interrupt flag should be restored").isTrue();
    }

    @Test
    @DisplayName("Integration: Book working day handles ChronosClientException during export")
    void testBookWorkingDayChronosMCP_HandlesClientExceptionOnExport() {
        timesheetsClient.setExportException(new ChronosClientException("Timesheet not found"));

        String bookingJson = "{\"projectName\":\"Project\",\"accountName\":\"Account\",\"comment\":\"Work\",\"duration\":\"PT7H\"}";

        assertThatThrownBy(() -> chronosTool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                Collections.singletonList(bookingJson),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Failed to book working day")
                .hasMessageContaining("2024-11-26")
                .hasCauseInstanceOf(ChronosClientException.class);
    }

    @Test
    @DisplayName("Integration: Book working day validates contract is required")
    void testBookWorkingDayChronosMCP_ValidatesContract() {
        String bookingJson = "{\"projectName\":\"Project\",\"accountName\":\"Account\",\"comment\":\"Work\",\"duration\":\"PT7H\"}";

        assertThatThrownBy(() -> chronosTool.bookWorkingDayChronosMCP(
                null,
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                Collections.singletonList(bookingJson),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Contract number is required");

        assertThat(timesheetsClient.exportCallCount).isZero();
    }

    @Test
    @DisplayName("Integration: Book working day handles null export result")
    void testBookWorkingDayChronosMCP_HandlesNullExportResult() {
        timesheetsClient.setExportResult(null);

        String bookingJson = "{\"projectName\":\"Project\",\"accountName\":\"Account\",\"comment\":\"Work\",\"duration\":\"PT7H\"}";

        assertThatThrownBy(() -> chronosTool.bookWorkingDayChronosMCP(
                "12345",
                "2024-11-26",
                "09:00",
                "17:00",
                "PT1H",
                Collections.singletonList(bookingJson),
                "token"
        ))
                .isInstanceOf(ChronosMCPException.class)
                .hasMessageContaining("Failed to export timesheet")
                .hasMessageContaining("12345")
                .hasMessageContaining("2024-11");

        assertThat(timesheetsClient.exportCallCount).isEqualTo(1);
    }

    // ===== Happy Path Tests =====
    //
    // Note: Due to Java 25 module restrictions and the fact that Chronos model classes are final,
    // we cannot create realistic instances of ExportTimesheet, TimesheetListingEntry, etc.
    // These happy path tests focus on successful execution flows with the data we can create.

    @Test
    @DisplayName("Happy Path: List projects returns empty list successfully")
    void testListProjectsChronosMCP_SuccessfulEmptyResponse() {
        projectsClient.setListProjectsResult(Collections.emptyList());

        List<ProjectListingEntry> result = chronosTool.listProjectsChronosMCP(2024, 11, "auth-token");

        assertThat(result).isNotNull().isEmpty();
        assertThat(projectsClient.listProjectsCallCount).isEqualTo(1);
        assertThat(projectsClient.lastYearMonth).isEqualTo(YearMonth.of(2024, 11));
        assertThat(projectsClient.lastToken).isEqualTo("auth-token");
    }

    @Test
    @DisplayName("Happy Path: Get timesheets returns empty list successfully")
    void testGetTimesheetsChronosMCP_SuccessfulEmptyResponse() {
        timesheetsClient.setListResult(Collections.emptyList());

        List<TimesheetListingEntryDto> result = chronosTool.getTimesheetsChronosMCP(2024, 11, "auth-token");

        assertThat(result).isNotNull().isEmpty();
        assertThat(timesheetsClient.listCallCount).isEqualTo(1);
        assertThat(timesheetsClient.lastYearMonth).isEqualTo(YearMonth.of(2024, 11));
        assertThat(timesheetsClient.lastToken).isEqualTo("auth-token");
    }

    @Test
    @DisplayName("Happy Path: Retrieve user context with empty timesheets successfully")
    void testRetrieveUserContextChronosMCP_SuccessfulEmptyContext() {
        timesheetsClient.setListResult(Collections.emptyList());

        UserContext result = chronosTool.retrieveUserContextChronosMCP("auth-token");

        assertThat(result).isNotNull();
        assertThat(result.activeContracts()).isEmpty();
        assertThat(result.recentlyBookedProjects()).isEmpty();
        assertThat(result.recentlyBookedAccounts()).isEmpty();
        assertThat(timesheetsClient.listCallCount).isEqualTo(1);
    }

    // ===== Fake Implementations =====

    /**
     * Fake implementation of ProjectsClient for testing purposes.
     */
    private static class FakeProjectsClient implements ProjectsClient {
        private List<ProjectListingEntry> listProjectsResult;
        private InterruptedException listProjectsException;
        int listProjectsCallCount = 0;
        YearMonth lastYearMonth;
        String lastToken;

        void setListProjectsResult(List<ProjectListingEntry> result) {
            this.listProjectsResult = result;
        }

        void setListProjectsException(InterruptedException exception) {
            this.listProjectsException = exception;
        }

        @Override
        public List<ProjectListingEntry> listProjects(YearMonth yearMonth, String token) throws InterruptedException {
            listProjectsCallCount++;
            lastYearMonth = yearMonth;
            lastToken = token;
            if (listProjectsException != null) {
                throw listProjectsException;
            }
            return listProjectsResult;
        }

        @Override
        public String listProjectsRaw(YearMonth yearMonth, String token) throws InterruptedException {
            // Stub for raw API call - not used in tests
            throw new UnsupportedOperationException("listProjectsRaw not implemented in fake");
        }
    }

    /**
     * Fake implementation of TimesheetsClient for testing purposes.
     */
    private static class FakeTimesheetsClient implements TimesheetsClient {
        private List<TimesheetListingEntry> listResult;
        private InterruptedException listException;
        private ExportTimesheet exportResult;
        private InterruptedException exportException;
        private ChronosClientException exportClientException;

        int listCallCount = 0;
        int exportCallCount = 0;
        int importCallCount = 0;
        YearMonth lastYearMonth;
        String lastToken;
        String lastContractId;

        void setListResult(List<TimesheetListingEntry> result) {
            this.listResult = result;
        }

        void setListException(InterruptedException exception) {
            this.listException = exception;
        }

        void setExportResult(ExportTimesheet result) {
            this.exportResult = result;
        }

        void setExportException(InterruptedException exception) {
            this.exportException = exception;
            this.exportClientException = null;
        }

        void setExportException(ChronosClientException exception) {
            this.exportClientException = exception;
            this.exportException = null;
        }

        @Override
        public List<TimesheetListingEntry> list(YearMonth yearMonth, String token) throws InterruptedException {
            listCallCount++;
            lastYearMonth = yearMonth;
            lastToken = token;
            if (listException != null) {
                throw listException;
            }
            return listResult;
        }

        @Override
        public List<TimesheetListingEntry> list(java.time.Year year, String token) throws InterruptedException {
            // Stub for year-based list - not used in tests
            throw new UnsupportedOperationException("list(Year) not implemented in fake");
        }

        @Override
        public List<TimesheetListingEntry> list(String token) throws InterruptedException {
            // Stub for token-only list - not used in tests
            throw new UnsupportedOperationException("list(String) not implemented in fake");
        }

        @Override
        public String listRaw(YearMonth yearMonth, String token) throws InterruptedException {
            // Stub for raw API call - not used in tests
            throw new UnsupportedOperationException("listRaw not implemented in fake");
        }

        @Override
        public String listRaw(java.time.Year year, String token) throws InterruptedException {
            // Stub for raw API call - not used in tests
            throw new UnsupportedOperationException("listRaw(Year) not implemented in fake");
        }

        @Override
        public String listRaw(String token) throws InterruptedException {
            // Stub for raw API call - not used in tests
            throw new UnsupportedOperationException("listRaw(String) not implemented in fake");
        }

        @Override
        public ExportTimesheet export(String contractId, YearMonth yearMonth, String token) throws InterruptedException {
            exportCallCount++;
            lastContractId = contractId;
            lastYearMonth = yearMonth;
            lastToken = token;
            if (exportException != null) {
                throw exportException;
            }
            if (exportClientException != null) {
                throw exportClientException;
            }
            return exportResult;
        }

        @Override
        public String exportRaw(String contractId, YearMonth yearMonth, String token) throws InterruptedException {
            // Stub for raw API call - not used in tests
            throw new UnsupportedOperationException("exportRaw not implemented in fake");
        }

        @Override
        public Either<ImportErrors, ImportSuccess> importTimesheet(ImportTimesheet importTimesheet, String contractId, YearMonth yearMonth, String token) throws InterruptedException {
            importCallCount++;
            lastContractId = contractId;
            lastYearMonth = yearMonth;
            lastToken = token;
            // Return success by default for testing
            return Either.right(null);
        }

        @Override
        public Either<RawImportErrors, RawImportSuccess> importTimesheetRaw(String rawData, String contractId, YearMonth yearMonth, String token) throws InterruptedException {
            // Stub for raw API call - not used in tests
            throw new UnsupportedOperationException("importTimesheetRaw not implemented in fake");
        }
    }
}
