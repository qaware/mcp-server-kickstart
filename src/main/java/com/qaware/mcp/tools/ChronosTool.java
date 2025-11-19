package com.qaware.mcp.tools;

import com.qaware.mcp.McpParam;
import com.qaware.mcp.McpTool;
import de.qaware.qaerp.chronos.client.api.ChronosClientConfig;
import de.qaware.qaerp.chronos.client.api.ProjectsClient;
import de.qaware.qaerp.chronos.client.impl.*;
import de.qaware.qaerp.chronos.client.impl.projects.ProjectsClientImpl;
import de.qaware.qaerp.chronos.client.impl.projects.ProjectsConnector;
import de.qaware.qaerp.chronos.client.model.ProjectListingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.util.List;

/**
 * A MCP tool enabling AI agents to interact with the QAware timekeeping and project management system Chronos.
 *
 * It currently provides the ability to fetch all projects that are available to a given user.
 */
public class ChronosTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronosTool.class);

    private final ProjectsClient projectsClient;

    public ChronosTool() {
        this.projectsClient = new ProjectsClientImpl(new ProjectsConnector(new JsonReader(),
                new ChronosApi(
                        ChronosClientConfig.builder()
                                .chronosApiBaseUrl("https://zeit.qaware.de")
                                .httpClientTimeoutSeconds(20)
                                .build(),
                        new JsonPrinter(),
                        new RequestAuthorizer(
                                new AccessTokenProvider()
                        )
                )));
        LOGGER.info("Starting ChronosTool");
    }


    /**
     * Retrieves the projects available for the given year and month.
     * If any of the required parameters are null, search for the current month
     *
     * @param year year-part of the search
     * @param month month-part of the search
     * @return the source code of the requested class as a UTF-8 string
     */

    @McpTool("Retrieves the projects available for the given year and month. Use this to retrieve a list of projects and accounts")
    public List<ProjectListingEntry> listProjects(@McpParam(name = "year", description = "year as int in YYYY format") Integer year,
                                                  @McpParam(name = "month", description = "month as int in MM format, January is 1") Integer month) {
        YearMonth fetchedYearMonth;
        if (year == null || month == null ) {
            fetchedYearMonth = YearMonth.now();
        } else {
            fetchedYearMonth = YearMonth.of(year, month);
        }

        LOGGER.debug("Fetching projects for year month {}", fetchedYearMonth);
        try {
            return projectsClient.listProjects(fetchedYearMonth, "<my token>");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
