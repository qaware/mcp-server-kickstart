package com.qaware.mcp.chronos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaware.mcp.McpParam;
import com.qaware.mcp.McpTool;
import de.qaware.qaerp.chronos.client.api.ProjectsClient;
import de.qaware.qaerp.chronos.client.api.TimesheetsClient;
import de.qaware.qaerp.chronos.client.api.error.ChronosClientException;
import de.qaware.qaerp.chronos.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * A MCP tool enabling AI agents to interact with the QAware timekeeping and project management system Chronos.
 * <p>
 * It currently provides the ability to fetch all projects that are available to a given user.
 */
public class ChronosTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronosTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ProjectsClient projectsClient;
    private final TimesheetsClient timesheetsClient;

    /**
     * Creates a ChronosTool with default configuration.
     */
    public ChronosTool() {
        this(new ChronosClientFactory());
    }

    /**
     * Creates a ChronosTool with the given factory.
     *
     * @param factory The factory to use for creating Chronos clients
     */
    public ChronosTool(ChronosClientFactory factory) {
        this.projectsClient = factory.createProjectsClient();
        this.timesheetsClient = factory.createTimesheetsClient();
        LOGGER.info("Starting ChronosTool");
    }

    /**
     * Creates a ChronosTool with specific client instances (for testing).
     *
     * @param projectsClient The ProjectsClient to use
     * @param timesheetsClient The TimesheetsClient to use
     */
    ChronosTool(ProjectsClient projectsClient, TimesheetsClient timesheetsClient) {
        this.projectsClient = projectsClient;
        this.timesheetsClient = timesheetsClient;
        LOGGER.info("Starting ChronosTool with injected clients");
    }

    /**
     * Retrieves the projects available for the given year and month.
     * If any of the required parameters are null, search for the current month
     *
     * @param year year-part of the search
     * @param month month-part of the search
     * @param bearerToken authentication token for the user (Google account as JWT token)
     * @return List of Projects including their accounts
     */
    @McpTool("Retrieves the projects available for the given year and month. Use this to retrieve a list of projects and accounts")
    public List<ProjectListingEntry> listProjectsChronosMCP(@McpParam(name = "year", description = "year as int in YYYY format") Integer year,
                                                  @McpParam(name = "month", description = "month as int in MM format, January is 1") Integer month,
                                                  @McpParam(name = "authorizationToken", description = "authentication token for the current user") String bearerToken) {
        YearMonth fetchedYearMonth = buildYearMonth(year, month);

        LOGGER.debug("Fetching projects for year month {}", fetchedYearMonth);
        try {
            return projectsClient.listProjects(fetchedYearMonth, bearerToken);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChronosMCPException("Failed to fetch projects for " + fetchedYearMonth, e);
        }
    }

    /**
     * Retrieves the relevant Information for the currently connected user.
     * This includes the active contracts and the recently booked projects and accounts
     *
     * @param bearerToken authentication token for the user (Google account as JWT token)
     * @return Recently used contracts, projects and accounts
     */
    @McpTool("Retrieves relevant Information for the currently connected user. This includes the active contracts and the recently booked projects and accounts")
    public UserContext retrieveUserContextChronosMCP(@McpParam(name = "authorizationToken", description = "authentication token for the current user") String bearerToken) {
        try {
            List<String> contracts;
            List<TimesheetListingEntryDto> currentTimesheets = timesheetsClient.list(YearMonth.now(), bearerToken)
                    .stream()
                    .map(TimesheetListingEntryDto::of)
                    .toList();
            contracts = currentTimesheets.stream().map(TimesheetListingEntryDto::contract).toList();

            Set<String> recentlyBookedProjects = new TreeSet<>();
            Set<String> recentlyBookedAccounts = new TreeSet<>();
            for (String contract : contracts) {
                // Read data for current month
                collectRecentBookingData(bearerToken, contract, YearMonth.now(), recentlyBookedProjects, recentlyBookedAccounts);
                // Read data for previous month
                collectRecentBookingData(bearerToken, contract, YearMonth.now().minusMonths(1), recentlyBookedProjects, recentlyBookedAccounts);
            }

            return new UserContext(contracts, recentlyBookedProjects.stream().toList(), recentlyBookedAccounts.stream().toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChronosMCPException("Failed to retrieve user context", e);
        }
    }

    private void collectRecentBookingData(String bearerToken,
                                          String contract,
                                          YearMonth monthToEvaluate,
                                          Set<String> recentlyBookedProjects,
                                          Set<String> recentlyBookedAccounts) throws InterruptedException {
        try {
            ExportTimesheet currentTimesheet;
            currentTimesheet = timesheetsClient.export(contract, monthToEvaluate, bearerToken);
            for (ExportBooking projectInTimesheet : currentTimesheet.getBookings()) {
                recentlyBookedProjects.add(projectInTimesheet.getProjectName());
                recentlyBookedAccounts.add(projectInTimesheet.getAccountName());
            }
        } catch (ChronosClientException e) {
            LOGGER.debug("Could not retrieve timesheet data for contract {} in {}: {}. This is expected if no data exists.",
                    contract, monthToEvaluate, e.getMessage());
        }
    }

    /**
     * Retrieves the timesheet for the given year and month.
     * If any of the required parameters are null, search for the current month
     *
     * @param year year-part of the search
     * @param month month-part of the search
     * @param bearerToken authentication token for the user (Google account as JWT token)
     * @return Timesheet(s) of the given month for the active user
     */
    @McpTool("Retrieves the users timesheet for the given year and month. Use this to retrieve the current work times and time bookings for the current user.")
    public List<TimesheetListingEntryDto> getTimesheetsChronosMCP(@McpParam(name = "year", description = "year as int in YYYY format") Integer year,
                                                    @McpParam(name = "month", description = "month as int in MM format, January is 1") Integer month,
                                                    @McpParam(name = "authorizationToken", description = "authentication token for the current user") String bearerToken) {
        YearMonth fetchedYearMonth = buildYearMonth(year, month);

        LOGGER.debug("Fetching timesheets for year month {}", fetchedYearMonth);
        try {
            return timesheetsClient.list(fetchedYearMonth, bearerToken)
                    .stream()
                    .map(TimesheetListingEntryDto::of)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChronosMCPException("Failed to fetch timesheets for " + fetchedYearMonth, e);
        }
    }

    /**
     * Book the time entries of a given day into the respective timesheet
     *
     * @param contract               Contract number of the employee to be used for the booking
     * @param workingDayString       day to edit/book
     * @param workingStartTimeString time the workday started
     * @param workingEndTimeString   time the workday ended
     * @param breakDurationString    total duration of breaks during the workday
     * @param bearerToken            authentication token for the user (Google account as JWT token)
     * @return true if the booking was successful
     */
    @McpTool("Book the time entries of a given day into the respective timesheet")
    public Boolean bookWorkingDayChronosMCP(@McpParam(name = "contract", description = "Contract number of the employee to be used for the booking") String contract,
                                            @McpParam(name = "workingDay", description = "the day to edit/book in YYY-MM-dd format") String workingDayString,
                                            @McpParam(name = "workingStartTime", description = "time the workday started in HH:mm format") String workingStartTimeString,
                                            @McpParam(name = "workingEndTime", description = "time the workday ended in HH:mm format") String workingEndTimeString,
                                            @McpParam(name = "breakDuration", description = "total duration of breaks during the workday in ISO-8601 duration format PnDTnHnMn.nS") String breakDurationString,
                                            @McpParam(name = "bookings", description = "List of bookings, each booking as JSON-object with the fields " +
                                                    "'projectName' (string), accountName (string), duration (ISO-8601 duration format PnDTnHnMn.nS) and comment (string)") List<String> bookings,
                                            @McpParam(name = "authorizationToken", description = "authentication token for the current user") String bearerToken) {

        // Validate required parameters
        if (contract == null || contract.isBlank()) {
            throw new ChronosMCPException("Contract number is required");
        }
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new ChronosMCPException("Authorization token is required");
        }
        if (bookings == null || bookings.isEmpty()) {
            throw new ChronosMCPException("At least one booking is required");
        }

        LocalDate workingDay;
        LocalTime workingStartTime;
        LocalTime workingEndTime;
        Duration breakDuration;

        try {
            workingDay = LocalDate.parse(workingDayString);
            workingStartTime = LocalTime.parse(workingStartTimeString);
            workingEndTime = LocalTime.parse(workingEndTimeString);
            breakDuration = Duration.parse(breakDurationString);
        } catch (DateTimeParseException e) {
            throw new ChronosMCPException("Invalid date/time format provided: " + e.getMessage(), e);
        }

        // Validate parsed values
        if (workingStartTime.isAfter(workingEndTime)) {
            throw new ChronosMCPException("Working start time (" + workingStartTime + ") must be before end time (" + workingEndTime + ")");
        }
        if (breakDuration.isNegative()) {
            throw new ChronosMCPException("Break duration cannot be negative");
        }
        Duration totalWorkTime = Duration.between(workingStartTime, workingEndTime);
        if (breakDuration.compareTo(totalWorkTime) >= 0) {
            throw new ChronosMCPException("Break duration (" + breakDuration + ") cannot be >= total work time (" + totalWorkTime + ")");
        }

        try {
            ExportTimesheet currentTimesheet = timesheetsClient.export(contract, YearMonth.from(workingDay), bearerToken);
            if (currentTimesheet == null) {
                throw new ChronosMCPException("Failed to export timesheet for contract " + contract + " and month " + YearMonth.from(workingDay) + ": received null response from API");
            }

            List<ImportWorkingHour> updatedWorkingHours = getUpdatedWorkingHours(workingDay, workingStartTime, workingEndTime, breakDuration, currentTimesheet);

            List<ImportBooking> newDayBookings = mapUpdatedDayBookings(workingDay, bookings);
            List<ImportBooking> updatedBookings = getUpdatedBookings(workingDay, newDayBookings, currentTimesheet);

            ImportTimesheet updatedTimesheet = ImportTimesheet.builder()
                    .workingHours(updatedWorkingHours)
                    .bookings(updatedBookings)
                    .build();

            LOGGER.trace("Updating working day timesheet {}", updatedTimesheet);
            timesheetsClient.importTimesheet(
                    updatedTimesheet,
                    contract,
                    YearMonth.from(workingDay),
                    bearerToken
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChronosMCPException("Booking operation was interrupted for " + workingDayString, e);
        } catch (ChronosClientException e) {
            throw new ChronosMCPException("Failed to book working day " + workingDayString + ": " + e.getMessage(), e);
        }
        return true;
    }

    private static YearMonth buildYearMonth(Integer year, Integer month) {
        YearMonth fetchedYearMonth;
        if (year == null || month == null ) {
            fetchedYearMonth = YearMonth.now();
        } else {
            fetchedYearMonth = YearMonth.of(year, month);
        }
        return fetchedYearMonth;
    }
    private static List<ImportWorkingHour> getUpdatedWorkingHours(LocalDate workingDay, LocalTime workingStartTime, LocalTime workingEndTime, Duration breakDuration, ExportTimesheet currentTimesheet) {
        ImportWorkingHour currentDayWorkingHours = ImportWorkingHour.builder()
                .workingDay(workingDay.getDayOfMonth())
                .start(workingStartTime)
                .end(workingEndTime)
                .breaks(breakDuration)
                .build();
        return currentTimesheet.getWorkingHours().stream()
                .map(exportWorkingHour -> {
                    if (exportWorkingHour.getWorkingDay().equals(currentDayWorkingHours.getWorkingDay())) {
                        return currentDayWorkingHours;
                    } else {
                        return ImportWorkingHour.builder()
                                .workingDay(exportWorkingHour.getWorkingDay())
                                .start(exportWorkingHour.getStart())
                                .end(exportWorkingHour.getEnd())
                                .breaks(exportWorkingHour.getBreaks())
                                .build();
                    }
                })
                .toList();
    }

    private static List<ImportBooking> getUpdatedBookings(LocalDate workingDay, List<ImportBooking> currentDayBookings, ExportTimesheet currentTimesheet)
        throws ChronosMCPException {

        ArrayList<ImportBooking> updatedBookings = new ArrayList<>(currentTimesheet.getBookings().stream()
                .filter(exportBooking -> !exportBooking.getWorkingDay().equals(workingDay.getDayOfMonth()))
                .map(exportBooking -> ImportBooking.builder()
                        .workingDay(exportBooking.getWorkingDay())
                        .projectName(exportBooking.getProjectName())
                        .accountName(exportBooking.getAccountName())
                        .comment(exportBooking.getComment())
                        .duration(exportBooking.getDuration())
                        .build())
                .toList());
        updatedBookings.addAll(currentDayBookings);
        return updatedBookings;
    }

    private static List<ImportBooking> mapUpdatedDayBookings(LocalDate workingDay, List<String> bookingJsons) {
        return bookingJsons.stream()
                .map(bookingString ->
                        {
                            try {
                                return OBJECT_MAPPER.readValue(bookingString, ImportBookingDto.class);
                            } catch (JsonProcessingException e) {
                                throw new ChronosMCPException("Failed to parse booking JSON: '" + bookingString + "' - " + e.getMessage(), e);
                            }
                        }
                )
                .map(dto -> ImportBooking.builder()
                        .workingDay(workingDay.getDayOfMonth())
                        .projectName(dto.projectName())
                        .accountName(dto.accountName())
                        .comment(dto.comment())
                        .duration(Duration.parse(dto.duration()))
                        .build()
                )
                .toList();
    }
}
