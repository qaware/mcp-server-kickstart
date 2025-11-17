package com.qaware.mcp;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;

/** MCP Stdio Server to expose tools via STDIO transport making it suitable for integration with MCP clients via process spawning. */
enum McpStdioServer {

    ;


    static McpSyncServer build(String serverName, String serverVersion, Object... tools) {
        return McpSseServlet.build(serverName, serverVersion, new StdioServerTransportProvider(McpTools.MCP_JSON_MAPPER), tools);
    }

}
