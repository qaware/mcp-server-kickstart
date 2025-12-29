package com.qaware.mcp.tools.knowledge.nlp;

import com.carrotsearch.hppc.LongContainer;
import com.carrotsearch.hppc.LongHashSet;
import com.carrotsearch.hppc.LongSet;

public class FilterStopWords extends Filter {

    private final LongContainer stopWordHashes;


    public FilterStopWords(Tokens tokens, LongContainer sharedStopWordHashes) {
        super(tokens);

        stopWordHashes = sharedStopWordHashes;
    }


    public static LongContainer createWordHashes(Tokens tokens, CharSequence... stopWords) {
        LongSet wordHashes = new LongHashSet();

        for (CharSequence chars : stopWords) {
            for (tokens.reset(chars); tokens.next();) wordHashes.add(tokens.hash());
        }

        return wordHashes;
    }


    @Override
    public boolean next() {
        while (super.next()) if (! stopWordHashes.contains(hash())) return true;

        return false;
    }

}
