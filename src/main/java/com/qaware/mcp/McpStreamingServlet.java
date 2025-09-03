package com.qaware.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static io.modelcontextprotocol.server.McpServer.sync;

// yeah, lots of code duplication between McpSseServlet and McpStreamingServlet because there are no common interfaces
enum McpStreamingServlet {

    ;


    private static final Logger LOGGER = LoggerFactory.getLogger(McpStreamingServlet.class);


    static HttpServletStatelessServerTransport getServlet(String serverName, String serverVersion, Object... tools) {
        HttpServletStatelessServerTransport transportProvider =
                HttpServletStatelessServerTransport.builder().objectMapper(McpTools.OBJECT_MAPPER).build();

        McpStreamingServlet.build(sync(transportProvider), serverName, serverVersion, tools);

        return transportProvider;
    }


    private static void build(McpServer.StatelessSyncSpecification spec, String serverName, String serverVersion, Object... tools) {
        McpStatelessSyncServer server = spec
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


    private static McpStatelessServerFeatures.SyncToolSpecification newToolSpec(Object tool, Method method, McpTool mcpDesc) {
        return McpStatelessServerFeatures.SyncToolSpecification.builder()
                .tool(McpTools.getTool(method, mcpDesc))
                .callHandler(McpTools.getCallHandler(tool, method))
                .build();
    }

}
