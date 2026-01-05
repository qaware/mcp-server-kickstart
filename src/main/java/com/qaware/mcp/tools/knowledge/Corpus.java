package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.tools.knowledge.nlp.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

class Corpus {

    private static final Logger LOGGER = LoggerFactory.getLogger(Corpus.class);

    private static final Recycler<Tokens> TOKENS_RECYCLER = new Recycler<>(Linguistic::newFilter, null);

    private final Dictionary dictionary = new Dictionary();

    private final Set<String> seen = new HashSet<>();

    private final Map<String, SimpleDoc> docs = new TreeMap<>();

    private final Consumer<Consumer<Location>> scanner;


    Corpus(Consumer<Consumer<Location>> scanner) {
        this.scanner = scanner;
    }


    synchronized String getPassages(String query, int limit) {
        long startNanoTime = System.nanoTime();

        updateCorpus();

        startNanoTime = measure(startNanoTime, "scan");

        FloatHistogram floatHistogram = new FloatHistogram();

        docs.values().forEach(SimpleDoc::resetScores);

        startNanoTime = measure(startNanoTime, "clear");

        score(query);
        startNanoTime = measure(startNanoTime, "score");

        docs.values().parallelStream().forEach(SimpleDoc::smooth);
        docs.values().forEach(doc -> doc.update(floatHistogram));

        // coole mögliche erweiterung: ich gucke mir die top 1% passagen an und hole mir daraus weitere tokens, die ich dann nochmal scoren lasse
        // -> automatic query expansion

        float threshold = Math.max(floatHistogram.getThreshold(limit), 0.00001f); // minimal threshold to avoid fetching everything
        startNanoTime = measure(startNanoTime, "threshold");

        int total = 1;
        int sum = 1;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, SimpleDoc> entry : docs.entrySet()) {
            SimpleDoc simpleDoc = entry.getValue();
            simpleDoc.append(stringBuilder, threshold, entry.getKey());
            for (float score : simpleDoc.scores) if (score > 0) { sum += simpleDoc.length(); break; }
            total += simpleDoc.length();
        }

        String result = stringBuilder.toString();

        measure(startNanoTime, "paragraphs");

        measure(startNanoTime, "🔴 " + query + " --> " + limit + " " + result.length() + "/" + sum + "/" + total + " " + result.length() * 1000 / sum / 10f + "%");

        return result;
    }


    synchronized String getAll() {
        updateCorpus();

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, SimpleDoc> entry : docs.entrySet()) {
            SimpleDoc simpleDoc = entry.getValue();
            simpleDoc.append(stringBuilder, 0, simpleDoc.scores.length, entry.getKey());
        }

        return stringBuilder.toString();
    }


    private void updateCorpus() {
        scanner.accept(this::addLocation);
        docs.keySet().stream().filter(x -> ! seen.contains(x)).forEach(x -> LOGGER.debug("DEL: {}", x));
        docs.keySet().retainAll(seen);
        seen.clear();
    }


    private void addLocation(Location location) {
        String id = location.getId();

        synchronized (seen) {
            seen.add(id);
        }

        long lastMod = location.getVersion();

        SimpleDoc simpleDoc = get(id);
        if (simpleDoc != null && simpleDoc.lastMod() == lastMod) return;

        long startNano = System.nanoTime();

        Tokens tokens = TOKENS_RECYCLER.get().reset(location.getChars());
        simpleDoc = new SimpleDoc(lastMod, dictionary, tokens);
        TOKENS_RECYCLER.recycle(tokens);
        LOGGER.info("ADD/MOD: {} {}ms", id, (System.nanoTime() - startNano) / 1_000_000f);

        put(id, simpleDoc);
    }


    private void score(String chars) {
        Tokens tokens = TOKENS_RECYCLER.get().reset(chars);

        while (tokens.next()) {
            int df = Linguistic.getDF(tokens.hash());
            float score = (float) (5 * Math.log(3_000_000.0 / (1 + df)));

            LOGGER.info("{} {} {}", tokens, df, score);

            int tokenId = dictionary.get(tokens);
            if (tokenId >= 0) {
                docs.values().parallelStream().forEach(doc -> doc.addScore(tokenId, score));
            }
        }

        TOKENS_RECYCLER.recycle(tokens);
    }


    private static long measure(long startNanoTime, String string) {
        long endNanoTime = System.nanoTime();

        float millis = (System.nanoTime() - startNanoTime) / 1_000_000f;
        LOGGER.debug("{}... {}ms", string, millis);

        return endNanoTime;
    }


    private SimpleDoc get(String id) {
        synchronized (docs) {
            return docs.get(id);
        }
    }


    private void put(String id, SimpleDoc simpleDoc) {
        synchronized (docs) {
            docs.put(id, simpleDoc);
        }
    }

}
