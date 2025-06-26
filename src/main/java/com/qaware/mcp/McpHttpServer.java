package com.qaware.mcp;

import java.util.Map;

/*
jmcp com.qaware.mcp.McpHttpServer
*/
public class McpHttpServer {

    public interface BaseMcp {

        @McpTool("")
        Map<String, String> other();

    }

    public abstract static class Father implements BaseMcp {

        @McpTool("")
        void misc() {
            // action here
        }

    }


    public static class Tool extends Father {

        @McpTool("Gets class summaries for specified class")
        Map<String, String> getClassSummaries(
            @McpParam(name="fqns", description="The class names") String... fqns) {
            return Map.of("hello", "world"); //XXX
        }


        @McpTool("Greets a person")
        public String greetMe(@McpParam(name="name", description="The person's name") String name) {
            return "Hallo " + name;
        }


        @Override
        public Map<String, String> other() {
            // TODO Auto-generated method stub
            return null;
        }

    }


    public static void main(String[] args) throws Exception {
        McpServer.create().addTool(new Tool()).start();
    }

}