package com.qaware.mcp;

import java.util.Arrays;
import java.util.List;

/**
 * The {@code Server} class is the main entry point for starting the MCP server application.
 * <p>
 * This class is responsible for initializing and configuring the {@link McpServer} instance.
 * It parses command-line arguments to determine which communication protocol to use (SSE, stdio, or streaming),
 * and dynamically loads tool classes to be registered with the server. If no tool class is specified via arguments,
 * it defaults to loading the {@code HelloWorldTools} inner class, which provides basic demonstration tools.
 * <p>
 * Internal logic overview:
 * <ul>
 *   <li>Creates an {@link McpServer} instance using a factory method.</li>
 *   <li>Processes command-line flags ("--sse", "--stdio", "--streaming") to configure the server's communication protocol.</li>
 *   <li>If no tool class is specified, defaults to {@code HelloWorldTools}.</li>
 *   <li>Uses reflection to instantiate and register each specified tool class with the server.</li>
 *   <li>Starts the server, making the registered tools available for remote invocation.</li>
 * </ul>
 * <p>
 * The class also contains a static inner class {@code HelloWorldTools} that demonstrates basic tool functionality
 * such as greeting, addition, and returning example items.
 */
public class Server {

    public static void main(String[] args) {
        McpServer mcpServer = McpServer.create();

        args = handle("--sse"      , () -> mcpServer.useSSE()      , args);
        args = handle("--stdio"    , () -> mcpServer.useStdio()    , args);
        args = handle("--streaming", () -> mcpServer.useStreaming(), args);

        if (args.length == 0) {
            args = new String[] { HelloWorldTools.class.getName() };
        }

        for (String className : args) {
            mcpServer.addTool(Reflection.newInstance(className));
        }

        mcpServer.start();
    }


    private static String[] handle(String flag, Runnable action, String[] args) {
        if (args.length != 0 && args[0].equalsIgnoreCase(flag)) {
            action.run();
            return Arrays.copyOfRange(args, 1, args.length);
        }

        return args;
    }


    public static class HelloWorldTools {

        @McpTool("Says hello to someone")
        public String hello(@McpParam(name = "name", description = "Name to greet") String name) {
            return "Hello, " + name + "! 👋🥳";
        }


        @McpTool("Adds two numbers together")
        public int add(@McpParam(name = "a", description = "First number") int a,
            @McpParam(name = "b", description = "Second number") int b) {
            return a + b;
        }


        @McpTool("Lists some example items")
        public List<String> getItems(
            @McpParam(name = "category", description = "Item category") String category) {
            return switch (category.toLowerCase()) {
                case "fruits" -> List.of("Apple", "Banana", "Orange");
                case "colors" -> List.of("Red", "Green", "Blue");
                default       -> List.of("Item1", "Item2", "Item3");
            };
        }

    }

}
