package com.qaware.mcp.tools.knowledge;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntHashMap;
import com.qaware.mcp.tools.knowledge.nlp.Tokens;

class SimpleDoc {

    private static final int NOT_FOUND = -1;

    private final IntIntHashMap lastPos = new IntIntHashMap();
    private final IntArrayList previous = new IntArrayList();

    /*XXX*/ float[] scores;

    private final Dictionary dictionary; // XXX das kann eigentlich weg, stattdesen nur mit Hashes arbeiten

    private final long lastMod;

    private final CharSequence source;
    private final IntArrayList beginEnd = new IntArrayList(); // XXX das ist Mist!!! zwei Arrays besser
    private final IntArrayList tokenIds = new IntArrayList();


    public SimpleDoc(long aLastMod, Dictionary aDictionary, Tokens tokens) {
        lastMod    = aLastMod;
        dictionary = aDictionary;
        source     = tokens.source();

        int pos = 0;
        while (tokens.next()) {
            int tokenId = dictionary.add(tokens.buffer(), 0, tokens.length());

            tokenIds.add(tokenId);

            previous.add(lastPos.getOrDefault(tokenId, NOT_FOUND));
            lastPos.put(tokenId, pos);

            beginEnd.add(tokens.begin(), tokens.end());

            pos++;
        }

        scores = new float[pos];
    }


    int length() {
        return source.length();
    }


    public long lastMod() {
        return lastMod;
    }


    @Override
    public String toString() {
        return print(new StringBuilder()).toString();
    }


    CharSequence word(int i) {
        return source.subSequence(getBegin(i), getEnd(i));
    }


    Appendable print(Appendable out) {
        try {
            int lastBegin = NOT_FOUND;

            for (int i = 0; i < scores.length; i++) {
                int begin = getBegin(i);
                if (begin != lastBegin) {
                    lastBegin = begin;
                    if (i != 0) out.append('\n');
                    out.append(i + "\t" + scores[i] + "\t" + source.subSequence(begin, getEnd(i)) + "\t");
                }
                out.append(" " + dictionary.get(getToken(i)) + "<" + getToken(i) + ">");
            }

            return out;

        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }


    private int getToken(int i) {
        return tokenIds.get(i);
    }


    void resetScores() {
        Arrays.fill(scores, 0);
    }


    void addScore(int tokenId, float score) {
        for (int pos = lastPos.getOrDefault(tokenId, NOT_FOUND); pos != NOT_FOUND; pos = previous.get(pos)) {

            int firstPos = pos;
            while (firstPos > 0 && getBegin(firstPos - 1) == getBegin(firstPos)) firstPos--;

            scores[firstPos] = Math.max(scores[firstPos], score);
        }
    }


    void update(FloatHistogram floatHistogram) {
        int lastBegin = NOT_FOUND;

        for (int i = 0; i < scores.length; i++) {
            int begin = getBegin(i);
            if (begin != lastBegin && scores[i] > 0) floatHistogram.increment(scores[i]);
            lastBegin = begin;
        }
    }


    int getBegin(int i) {
        return beginEnd.get(i * 2);
    }


    int getEnd(int i) {
        return beginEnd.get(i * 2 + 1);
    }


    Appendable append(Appendable appendable, float threshold, String file) {
        int lastBegin  = NOT_FOUND;
        int blockBegin = NOT_FOUND;

        for (int i = 0; i < scores.length; i++) {
            int begin = getBegin(i);
            if (begin == lastBegin) continue;
            lastBegin = begin;

            if (scores[i] >= threshold) {
                if (blockBegin == NOT_FOUND) blockBegin = begin;
            } else {
                file = append(appendable, blockBegin, i, file);
                blockBegin = NOT_FOUND;
            }
        }

        append(appendable, blockBegin, scores.length, file);

        return appendable;
    }


    String append(Appendable appendable, int blockBegin, int endPos, String file) {
        if (blockBegin == NOT_FOUND) return file;

        try {
            if (file != null) appendable.append("\n🟡 FILE/SOURCE: ").append(file).append("\n");

            appendable.append(source.subSequence(blockBegin, getEnd(endPos - 1))).append("\n➖➖\n");

            return null;

        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }


    public void smooth() {
        smooth(scores);
    }


    static void smooth(float[] scores) {
        IntArrayList scorePos = new IntArrayList();

        for (int i = 0; i < scores.length; i++) if (scores[i] > 0) scorePos.add(i);

        if (scorePos.isEmpty()) return;

        float[] scoresCopy = scores.clone(); // geht kompakter!!!

        final int MAX_DIST = 200;
        for (int i = 0; i < scorePos.size(); i++) {

            int pos = scorePos.get(i);

            double score = 0;

            for (int j = i; j >= 0; j--) {
                int pos2 = scorePos.get(j);
                int dist = pos - pos2;
                if (dist > MAX_DIST) break;

                score += scoresCopy[pos2] / (1 + dist);
            }

            for (int j = i + 1; j < scorePos.size(); j++) {
                int pos2 = scorePos.get(j);
                int dist = pos2 - pos;
                if (dist > MAX_DIST) break;

                score += scoresCopy[pos2] / (1 + dist);
            }

            scores[pos] = (float) score;
        }

        float slope = 0.1f;

        int r = 0;
        for (int i = -1; i < scorePos.size(); i++) {
            int l  = r;
            r = i == scorePos.size() - 1 ? scores.length - 1 : scorePos.get(i + 1);

            float ll = scores[l];
            float rr = scores[r] - slope * (r - l);

            for (int j = l; j <= r; j++) {
                scores[j] = Math.max(Math.max(ll, rr), 0);
                ll -= slope;
                rr += slope;
            }

        }
    }

}
