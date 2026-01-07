package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.McpParam;
import com.qaware.mcp.McpTool;
import com.qaware.mcp.tools.Config;

/*
cd /D R:\codebase\_MISC\_PSBOM

set MCP_KB_MAX_CONTENT=3000
set MCP_KB_ROOT=R:/codebase/_MISC/_PSBOM
set MCP_SLURP_ROOT=C:/Viechtbauer/Zeugs/Docs/Maya/Ixlu
jmcp com.qaware.mcp.Server com.qaware.mcp.tools.knowledge.McpKnowledgeTool com.qaware.mcp.tools.knowledge.McpSlurpTool

touch C:\Users\JörgViechtbauer\AppData\Local\github-copilot\intellij\mcp.json
*/
public class McpKnowledgeTool {

    private final Corpus corpus = new Corpus(new FileSystemScanner(Config.get(Config.ROOT, ".").split(";")));

    private final int limit = Config.getInt(Config.MAX_CONTENT, 3000);


    @McpTool("A helpful glossary / knowledge base you can query for terms or concepts. If you encounter a word term you do not know exactly or want to get some information, ALWAYS query the knowledge base first - example: query='<term>'.")
    private String query(@McpParam(name = "query", description = "the query for the information - separate the terms by space. Initially, only query for specific terms only, translate terms into both German and English if possible. Only if the initial search does not yield any result, search a second time using synonyms.") String query) {
        return corpus.getPassages(query, limit);
    }

}
