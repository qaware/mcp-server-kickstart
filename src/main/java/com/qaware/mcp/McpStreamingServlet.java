package com.qaware.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;
import jakarta.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static io.modelcontextprotocol.server.McpServer.sync;

// yeah, lots of code duplication between McpSseServlet and McpStreamingServlet because there are no common interfaces

/** MCP Streaming Servlet to expose tools via Streaming transport. */
enum McpStreamingServlet {

    ;


    private static final Logger LOGGER = LoggerFactory.getLogger(McpStreamingServlet.class);


    static Servlet getServlet(String serverName, String serverVersion, Object... tools) {
        HttpServletStatelessServerTransport transportProvider =
                HttpServletStatelessServerTransport.builder().jsonMapper(McpTools.MCP_JSON_MAPPER).build();

        build(serverName, serverVersion, transportProvider, tools);

        return transportProvider;
    }


    private static void build(String serverName, String serverVersion, McpStatelessServerTransport transportProvider, Object... tools) {
        McpStatelessSyncServer server = sync(transportProvider)
                .serverInfo(serverName, serverVersion)
                .capabilities(McpTools.SERVER_CAPABILITIES)
                .build();

        for (Object tool : tools) {
            LOGGER.info("Registering tools from: {}", tool.getClass().getSimpleName());
            addTool(server, tool);
        }
    }


    private static void addTool(McpStatelessSyncServer server, Object tool) {
        Map<Method, McpTool> mcpMethods = Reflection.getMethodsWithAnnotations(tool.getClass(), McpTool.class);

        mcpMethods.forEach((method, mcpDesc) -> server.addTool(newToolSpec(tool, method, mcpDesc)));
    }


    private static McpStatelessServerFeatures.SyncToolSpecification newToolSpec(Object tool, Method method, McpTool mcpTool) {
        return McpStatelessServerFeatures.SyncToolSpecification.builder()
                .tool(McpTools.getTool(method, mcpTool))
                .callHandler(McpTools.getCallHandler(tool, method))
                .build();
    }

}
