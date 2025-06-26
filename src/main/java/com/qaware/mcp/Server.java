package com.qaware.mcp;

import java.util.List;

public class Server {

    public static void main(String[] args) throws Exception {
        McpServer.create()
            .serverInfo("Hello World MCP Server", "1.0.0")
            .port(8090)
            .addTool(new HelloWorldTool())
            .start();
    }


    public static class HelloWorldTool {

        @McpTool("Says hello to someone")
        public String hello(@McpParam(name = "name", description = "Name to greet") String name) {
            return "Hello, " + name + "! 👋";
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
                default -> List.of("Item1", "Item2", "Item3");
            };
        }
    }

}