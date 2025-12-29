package com.qaware.mcp;

import io.modelcontextprotocol.server.McpSyncServer;
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

/**
 * MCP Server to expose tools via different transports (Streaming, SSE, Stdio).
 */
public class McpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpServer.class);

    private static final int TYPE_STREAMING = 0;
    private static final int TYPE_SSE       = 1;
    private static final int TYPE_STDIO     = 2;

    private final List<Object> tools = new ArrayList<>();

    private int port = 8090;

    private String serverName = "MCP Server";
    private String serverVersion = "1.0.0";

    private int transportType = TYPE_STREAMING;


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


    public McpServer useStreaming() {
        return setTransportType(TYPE_STREAMING);
    }


    public McpServer useSSE() {
        return setTransportType(TYPE_SSE);
    }


    public McpServer useStdio() {
        return setTransportType(TYPE_STDIO);
    }


    /**
     * Starts the MCP server with the configured settings.
     */
    public void start() {
        if (tools.isEmpty()) {
            LOGGER.warn("I guess they front, that's why I know my life is out of tool, fool! 🎶");
            LOGGER.info("💡 Yo, your server's empty - drop some tools in the house: .addTool(new YourTool())");
            return;
        }

        LOGGER.info("Creating MCP server '{}' v{}", serverName, serverVersion);

        Object[] toolsArray = tools.toArray();

        if (transportType == TYPE_STDIO) {
            startStdioServer(toolsArray);
            return;
        }

        Servlet servlet = transportType == TYPE_SSE
                ? McpSseServlet      .getServlet(serverName, serverVersion, toolsArray)
                : McpStreamingServlet.getServlet(serverName, serverVersion, toolsArray);

        startWebServer(port, servlet);
    }


    private void startStdioServer(Object... toolsArray) {
        McpSyncServer server = McpStdioServer.build(serverName, serverVersion, toolsArray);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("Shutting down MCP STDIO Server...");
                server.close();
            } catch (Exception e) {
                LOGGER.error("Error during shutdown", e);
            }
        }));

        LOGGER.info("MCP STDIO Server started successfully");
    }


    private McpServer setTransportType(int newTransportType) {
        transportType = newTransportType;
        return this;
    }


    private static void startWebServer(int port, Servlet servlet)  {
        try {
            QueuedThreadPool threadPool = new QueuedThreadPool(16);
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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
