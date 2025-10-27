package com.qaware.mcp.tools.knowledge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import com.qaware.mcp.McpParam;
import com.qaware.mcp.McpTool;
import com.qaware.mcp.tools.McpSourceTool;
import com.qaware.mcp.tools.knowledge.nlp.BytesDecoder;
import com.qaware.mcp.tools.knowledge.nlp.Tokens;

/*
cd /D R:\codebase\_MISC\_PSBOM

jmcp com.qaware.mcp.tools.knowledge.McpKnowledgeTool

jmcp com.qaware.mcp.Server com.qaware.mcp.tools.knowledge.McpKnowledgeTool

touch C:\Users\JörgViechtbauer\AppData\Local\github-copilot\intellij\mcp.json


Nutze die Knowledge DB. Wichtig: sie enthält deutsche und englische Dokumente, übersetze die Begriffe die Du anfragst immer in beide Sprachen.

Bespiel: Wenn Du nach "part" suchst, suche nach "part teil".

Gib bei Antworten auch immer an, wo Du die Information her hast (idealerweise als URL oder Dateipfad)
*/
public class McpKnowledgeTool {

    private static final Recycler<Tokens> TOKENS = new Recycler<>(McpKnowledgeTool::newFilter, null);

    private final Dictionary dictionary = new Dictionary();

    private final Map<Path, SimpleDoc> docs = new TreeMap<>();



    @McpTool("A super helpful glossary / knowledge base you can query for terms or concepts. If you encounter a word term you do not know exactly or want to get some information, ALWAYS(!!!) query the knowledge base first - example: query='<term> <synonym>'")
    private /*TODO?*/ String query(@McpParam(name = "query", description = "the query for the information you need - separate the terms by space - use synonyms as well") String query/*, int limit*/) {
        return query(query, 2_000);
    }


    private synchronized /*TODO?*/ String query(String query, int limit) {
        long startNanoTime = System.nanoTime();
        long startNanoTime0 = startNanoTime;

        scan(Path.of(".")); //LÖSCHUNGEN!!!!!!!!!!!!!
        startNanoTime = measure(startNanoTime, "scan");


        float threshold = 0;

        FloatHistogram floatHistogram = new FloatHistogram();

        docs.values().forEach(SimpleDoc::clear);

        startNanoTime = measure(startNanoTime, "clear");

        score(query);
        startNanoTime = measure(startNanoTime, "score");

        docs.values().parallelStream().forEach(SimpleDoc::smooth);
        docs.values().forEach(doc -> doc.update(floatHistogram));

        threshold = floatHistogram.getThreshold(limit);
        startNanoTime = measure(startNanoTime, "threshold");

        StringBuilder target = new StringBuilder();
        float t = threshold;
        docs.entrySet().forEach(e -> e.getValue().append(target, t, e.getKey().toAbsolutePath().toString()));

        String result = target.toString();
        measure(startNanoTime, "paragraphs");

//System.out.println(result);

        measure(startNanoTime0, "🔴🔴🔴🔴🔴🔴 " + query + " --> " + limit);
        return result;
    }


    private static long measure(long startNanoTime, String string) {
        long endNanoTime = System.nanoTime();

        float millis = (System.nanoTime() - startNanoTime) / 1_000_000f;
        System.out.println(string + "... " + millis + "ms");

        return endNanoTime;
    }


    private void score(String chars) {
        Tokens tokens = TOKENS.get().reset(chars);

        while (tokens.next()) {
            int df = Linguistic.getDF(tokens.hash());
            float score = (float) (2 * Math.log(3_000_000.0 / (1 + df)));
            System.out.println(tokens + " " + df + " " + score);

            int tokenId = dictionary.get(tokens);
            if (tokenId >= 0) {
                docs.values().parallelStream().forEach(doc -> doc.score(tokenId, 10)); //XXX
            }
        }

        TOKENS.recycle(tokens);
    }


    private void scan(Path rootPath) {
        McpSourceTool.scan(rootPath, McpKnowledgeTool::isTextFile, this::visitTextFile);
    }


    private void visitTextFile(Path path) {
        long lastMod = getLastMod(path);

        SimpleDoc simpleDoc = get(path);
        if (simpleDoc != null && simpleDoc.lastMod() == lastMod) return;

        byte[] bytes = McpSourceTool.readBytes(McpSourceTool.toURL(path).toString());

        Tokens tokens = TOKENS.get().reset(new BytesDecoder().reset(bytes));
        simpleDoc = new SimpleDoc(lastMod, dictionary, tokens);
        TOKENS.recycle(tokens);

        put(path, simpleDoc);
    }


    private SimpleDoc get(Path path) {
        synchronized (docs) {
            return docs.get(path);
        }
    }


    private void put(Path path, SimpleDoc simpleDoc) {
        synchronized (docs) {
            docs.put(path, simpleDoc);
        }
    }


    private static long getLastMod(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return -1;
        }
    }


    private static Tokens newFilter() {
        return Linguistic.newFilter();
    }


    private static boolean isTextFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".md") || fileName.endsWith(".adoc")  || fileName.endsWith(".txt");
    }

}
