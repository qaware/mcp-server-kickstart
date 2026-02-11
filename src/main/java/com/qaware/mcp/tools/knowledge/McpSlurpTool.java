package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.McpParam;
import com.qaware.mcp.McpTool;

public class McpSlurpTool {

    @SuppressWarnings("static-method")
    @McpTool("This method will slurp in a document base and return it as a single string. Only call this method, if the user commands you to SLURP data. Do not call this without the user asking for it. Just slurp the data, do NOT print it. Wait for further instructions.")
    private String slurp(@McpParam(name = "path", description = "Name to greet") String paths) {
        return new Corpus(new FileSystemScanner(paths.split(";"))).getAll();
    }

}
