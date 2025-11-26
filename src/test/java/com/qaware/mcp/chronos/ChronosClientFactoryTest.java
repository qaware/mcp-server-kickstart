package com.qaware.mcp.chronos;

import de.qaware.qaerp.chronos.client.api.ProjectsClient;
import de.qaware.qaerp.chronos.client.api.TimesheetsClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChronosClientFactoryTest {

    @Test
    void testDefaultConstructor() {
        ChronosClientFactory factory = new ChronosClientFactory();

        assertThat(factory).isNotNull();
    }

    @Test
    void testConstructorWithConfig() {
        ChronosConfig config = new ChronosConfig();
        ChronosClientFactory factory = new ChronosClientFactory(config);

        assertThat(factory).isNotNull();
    }

    @Test
    void testCreateProjectsClient() {
        ChronosClientFactory factory = new ChronosClientFactory();

        ProjectsClient client = factory.createProjectsClient();

        assertThat(client).isNotNull();
    }

    @Test
    void testCreateTimesheetsClient() {
        ChronosClientFactory factory = new ChronosClientFactory();

        TimesheetsClient client = factory.createTimesheetsClient();

        assertThat(client).isNotNull();
    }

    @Test
    void testMultipleClientsCanBeCreated() {
        ChronosClientFactory factory = new ChronosClientFactory();

        ProjectsClient projectsClient1 = factory.createProjectsClient();
        ProjectsClient projectsClient2 = factory.createProjectsClient();
        TimesheetsClient timesheetsClient1 = factory.createTimesheetsClient();
        TimesheetsClient timesheetsClient2 = factory.createTimesheetsClient();

        assertThat(projectsClient1).isNotNull();
        assertThat(projectsClient2).isNotNull();
        assertThat(timesheetsClient1).isNotNull();
        assertThat(timesheetsClient2).isNotNull();
    }
}
