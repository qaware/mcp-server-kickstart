package com.qaware.mcp.chronos;

import de.qaware.qaerp.chronos.client.api.ChronosClientConfig;
import de.qaware.qaerp.chronos.client.api.ProjectsClient;
import de.qaware.qaerp.chronos.client.api.TimesheetsClient;
import de.qaware.qaerp.chronos.client.impl.*;
import de.qaware.qaerp.chronos.client.impl.projects.ProjectsClientImpl;
import de.qaware.qaerp.chronos.client.impl.projects.ProjectsConnector;
import de.qaware.qaerp.chronos.client.impl.timesheets.TimesheetsClientImpl;
import de.qaware.qaerp.chronos.client.impl.timesheets.TimesheetsConnector;

/**
 * Factory for creating Chronos client instances.
 * Encapsulates the creation logic for ProjectsClient and TimesheetsClient.
 */
public class ChronosClientFactory {
    private final ChronosConfig config;

    /**
     * Creates a factory with default configuration.
     */
    public ChronosClientFactory() {
        this(new ChronosConfig());
    }

    /**
     * Creates a factory with the given configuration.
     *
     * @param config The Chronos configuration to use
     */
    public ChronosClientFactory(ChronosConfig config) {
        this.config = config;
    }

    /**
     * Creates a new ProjectsClient instance.
     *
     * @return A configured ProjectsClient
     */
    public ProjectsClient createProjectsClient() {
        ChronosApi api = createChronosApi();
        return new ProjectsClientImpl(new ProjectsConnector(new JsonReader(), api));
    }

    /**
     * Creates a new TimesheetsClient instance.
     *
     * @return A configured TimesheetsClient
     */
    public TimesheetsClient createTimesheetsClient() {
        ChronosApi api = createChronosApi();
        return new TimesheetsClientImpl(
                new TimesheetsConnector(new JsonPrinter(), new JsonReader(), api)
        );
    }

    /**
     * Creates a ChronosApi instance with the configured settings.
     *
     * @return A configured ChronosApi
     */
    private ChronosApi createChronosApi() {
        return new ChronosApi(
                ChronosClientConfig.builder()
                        .chronosApiBaseUrl(config.getBaseUrl())
                        .httpClientTimeoutSeconds(config.getTimeoutSeconds())
                        .build(),
                new JsonPrinter(),
                new RequestAuthorizer(new AccessTokenProvider())
        );
    }
}
