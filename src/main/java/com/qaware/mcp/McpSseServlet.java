package com.qaware.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static io.modelcontextprotocol.server.McpServer.sync;

// yeah, lots of code duplication between McpSseServlet and McpStreamingServlet because there are no common interfaces
enum McpSseServlet {

    ;


    private static final Logger LOGGER = LoggerFactory.getLogger(McpSseServlet.class);


    static HttpServletSseServerTransportProvider getServlet(String serverName, String serverVersion, Object... tools) {
        HttpServletSseServerTransportProvider transportProvider =
                HttpServletSseServerTransportProvider.builder().objectMapper(McpTools.OBJECT_MAPPER).build();

        McpSseServlet.build(sync(transportProvider), serverName, serverVersion, tools);

        return transportProvider;
    }


    private static void build(McpServer.SingleSessionSyncSpecification spec, String serverName, String serverVersion, Object... tools) {
        McpSyncServer server = spec
                .serverInfo(serverName, serverVersion)
                .capabilities(McpTools.SERVER_CAPABILITIES)
                .build();

        for (Object tool : tools) {
            LOGGER.info("Registering tools from: {}", tool.getClass().getSimpleName());
            addTool(server, tool);
        }
    }


    private static void addTool(McpSyncServer mcpSyncServer, Object tool) {
        Map<Method, McpTool> mcpMethods = Reflection.getMethodsWithAnnotations(tool.getClass(), McpTool.class);

        mcpMethods.forEach((method, mcpDesc) -> mcpSyncServer.addTool(newSyncToolSpecification(tool, method, mcpDesc)));
    }


    private static McpServerFeatures.SyncToolSpecification newSyncToolSpecification(Object tool, Method method, McpTool mcpTool) {
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpTools.getTool(method, mcpTool))
                .callHandler(McpTools.getCallHandler(tool, method))
                .build();
    }

}
