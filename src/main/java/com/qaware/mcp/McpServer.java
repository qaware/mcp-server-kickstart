package com.qaware.mcp;

import jakarta.servlet.Servlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class McpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpServer.class);

    private int port = 8090;
    private String serverName = "MCP Server";
    private String serverVersion = "1.0.0";
    private boolean useSSE;
    private final List<Object> tools = new ArrayList<>();


    public static McpServer create() {
        return new McpServer();
    }


    public McpServer port(int port) {
        this.port = port;
        return this;
    }


    public McpServer serverInfo(String name, String version) {
        this.serverName = name;
        this.serverVersion = version;
        return this;
    }


    public McpServer addTool(Object tool) {
        tools.add(tool);
        return this;
    }


    /** in case the modern streaming api is not supported */
    public McpServer useSSE(boolean useSSE) {
        this.useSSE = useSSE;
        return this;
    }


    public void start() throws Exception {
        if (tools.isEmpty()) {
            LOGGER.warn("I guess they front, that's why I know my life is out of tool, fool! 🎶");
            LOGGER.info("💡 Yo, your server's empty - drop some tools in the house: .addTool(new YourTool())");
            return;
        }

        // (!) https://apidog.com/de/blog/java-mcp-server-guide-de/

/*
        in case we want to support STDIO

        McpSyncServer syncServer = io.modelcontextprotocol.server.McpServer.sync(new StdioServerTransportProvider())
                .serverInfo(serverName, serverVersion)
                .capabilities(McpTools.SERVER_CAPABILITIES)
                .build();

        McpSseServlet.addTool(syncServer, "hello");
/*/

        Servlet servlet = getServlet(serverName, serverVersion, useSSE, tools.toArray());

        startWebServer(port, servlet);
    }


    private static Servlet getServlet(String serverName, String serverVersion, boolean useSSE, Object... tools) {
        LOGGER.info("Creating MCP servlet '{}' v{}", serverName, serverVersion);

        return useSSE ? McpSseServlet      .getServlet(serverName, serverVersion, tools) :
                        McpStreamingServlet.getServlet(serverName, serverVersion, tools);
    }


    private static void startWebServer(int port, Servlet servlet) throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool(32);
        threadPool.setName("mcp-server");

        Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(servlet), "/*");

        server.setHandler(context);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("Shutting down MCP Server...");
                server.stop();
            } catch (Exception e) {
                LOGGER.error("Error during shutdown", e);
            }
        }));

        LOGGER.info("MCP Server started successfully on http://localhost:{}", port);
        LOGGER.info("Press Ctrl+C to stop the server");

        server.join();
    }

}
