package com.qaware.mcp;

import java.util.Arrays;
import java.util.List;
import com.qaware.mcp.tools.ChronosTool;

/**
 * The {@code Server} class is the main entry point for starting the MCP server application.
 * <p>
 * This class is responsible for initializing and configuring the {@link McpServer} instance.
 * <p>
 * Internal logic overview:
 * <ul>
 *   <li>Creates an {@link McpServer} instance using a factory method.</li>
 *   <li>Starts the server, making the registered tools available for remote invocation.</li>
 * </ul>
 */
public class Server {

    static void main(String[] args) {

        McpServer mcpServer = McpServer.create();

        mcpServer.serverInfo("Chronos MCP server", "0.1");
        mcpServer.addTool(new ChronosTool());
        mcpServer.start();
    }
}
