package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.McpTool;
import com.qaware.mcp.tools.Config;

public class McpSlurpTool {

    private final Corpus corpus = new Corpus(new FileSystemScanner(Config.get(Config.SLURP_ROOT, ".").split(";")));


    @McpTool("This method will slurp in a document base and return it as a single string. Only call this method, if the user commands you to SLURP all. Do not call this without the user asking for it.")
    private String slurp() {
        return corpus.getAll();
    }

}
