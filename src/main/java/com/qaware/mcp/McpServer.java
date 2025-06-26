package com.qaware.mcp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;

public class McpServer {

    private static final Logger log = LoggerFactory.getLogger(McpServer.class);

    private int port = 8090;
    private String serverName = "MCP Server";
    private String serverVersion = "1.0.0";
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


    public void start() throws Exception {
        if (tools.isEmpty()) {
            log.warn("I guess they front, that's why I know my life is out of tool, fool! 🎶");
            log.info("💡 Yo, your server's empty - drop some tools in the house: .addTool(new YourTool())");
            return;
        }

        Servlet mcpServlet = McpSupport.newMCPServlet(serverName, serverVersion, tools.toArray());

        startServer(port, mcpServlet);
    }


    private static void startServer(int port, Servlet servlet) throws Exception {
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
                log.info("Shutting down MCP Server...");
                server.stop();
            } catch (Exception e) {
                log.error("Error during shutdown", e);
            }
        }));

        log.info("MCP Server started successfully on http://localhost:{}", port);
        log.info("Press Ctrl+C to stop the server");

        server.join();
    }

}