package com.qaware.mcp;

import java.util.List;

public class Server {

    public static void main(String[] args) throws Exception {
        McpServer.create().addTool(new HelloWorldTools()).start();
    }


    public static class HelloWorldTools {

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