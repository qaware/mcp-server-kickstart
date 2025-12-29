package com.qaware.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import jakarta.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static io.modelcontextprotocol.server.McpServer.sync;

// yeah, lots of code duplication between McpSseServlet and McpStreamingServlet because there are no common interfaces

/** MCP SSE Servlet to expose tools via SSE transport. */
enum McpSseServlet {

    ;


    private static final Logger LOGGER = LoggerFactory.getLogger(McpSseServlet.class);


    static Servlet getServlet(String serverName, String serverVersion, Object... tools) {
        HttpServletSseServerTransportProvider transportProvider =
                HttpServletSseServerTransportProvider.builder().jsonMapper(McpTools.MCP_JSON_MAPPER).build();

        build(serverName, serverVersion, transportProvider, tools);

        return transportProvider;
    }


    static McpSyncServer build(String serverName, String serverVersion, McpServerTransportProvider mcpServerTransportProvider, Object... tools) {
        McpSyncServer server = sync(mcpServerTransportProvider)
                .serverInfo(serverName, serverVersion)
                .capabilities(McpTools.SERVER_CAPABILITIES)
                .build();

        for (Object tool : tools) {
            LOGGER.info("Registering tools from: {}", tool.getClass().getSimpleName());
            addTool(server, tool);
        }

        return server;
    }


    private static void addTool(McpSyncServer mcpSyncServer, Object tool) {
        Map<Method, McpTool> mcpMethods = Reflection.getMethodsWithAnnotations(tool.getClass(), McpTool.class);

        mcpMethods.forEach((method, mcpDesc) -> mcpSyncServer.addTool(newToolSpec(tool, method, mcpDesc)));
    }


    private static McpServerFeatures.SyncToolSpecification newToolSpec(Object tool, Method method, McpTool mcpTool) {
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpTools.getTool(method, mcpTool))
                .callHandler(McpTools.getCallHandler(tool, method))
                .build();
    }

}
