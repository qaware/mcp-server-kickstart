package com.qaware.mcp.tools.knowledge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.German2Stemmer;

import com.carrotsearch.hppc.LongHashSet;
import com.carrotsearch.hppc.LongSet;
import com.qaware.mcp.tools.knowledge.nlp.*;
import com.qaware.mcp.tools.knowledge.quantization.CountMinSketch;
import com.qaware.mcp.tools.knowledge.quantization.Quantizer;
import com.qaware.mcp.tools.knowledge.quantization.QuantizerTable;

/*
jmcp com.qaware.mcp.tools.knowledge.Linguistic
*/
public enum Linguistic {

    ;


    public static final int NO_WORD = -1;


    private static final LongSet stopWordHashes = new LongHashSet();

    private static final Quantizer quantizer = new QuantizerTable(QuantizerTable.exponential(256, 1.06f));

    private static final CountMinSketch dfMinSketch;


    static {
        for (long hash : (long[]) Serialization.readObject("stopword-hashes.dat")) stopWordHashes.add(hash);

        dfMinSketch = new CountMinSketch(4, Serialization.readObject("df-de-en-freq-count-min-sketch.dat"));
    }


    public static int getDf(String word, Filter filter) {
        return filter.reset(word).next() ? getDF(filter.hash()) : NO_WORD;
    }


    public static int getDF(long hash) {
        return (int) quantizer.get(dfMinSketch.getRaw(hash));
    }


    public static Filter newFilter() {
        Filter normalization =
            new FilterStopWords(
                new FilterGermanNormalization(
                    new FilterEnglishPossessive(
                        new FilterToLower(
                            new TokenizerSimple()))),
                stopWordHashes);

        FilterCombine filterCombine = new FilterCombine(normalization);
        filterCombine.combine(new FilterStemmerSnowball(filterCombine, new German2Stemmer()));
        filterCombine.combine(new FilterStemmerSnowball(filterCombine, new EnglishStemmer()));

        return new FilterDisambiguate(new FilterDeduplicate(filterCombine), dfMinSketch);
    }









    // XXX TEST ZEUG

    public static void main(String[] args) throws Exception {
        Filter filterEN =
                new FilterStemmerSnowball(
                        new FilterStopWords(
                                new FilterGermanNormalization(
                                        new FilterEnglishPossessive(
                                                new FilterToLower(
                                                        new TokenizerSimple()))),
                                stopWordHashes),
                        new EnglishStemmer());


        Filter filterDE =
                new FilterStemmerSnowball(
                        new FilterStopWords(
                                new FilterGermanNormalization(
                                        new FilterEnglishPossessive(
                                                new FilterToLower(
                                                        new TokenizerSimple()))),
                                stopWordHashes),
                        new German2Stemmer());


        Filter filter = newFilter();

        String text = "!!!  #Running# xxx run informationEN   AutomatioN   THE ihr das la grünste ruNNing going hellen inFOrmation hellste der letzte existS";
        if (args.length > 0) text = args[0];

        int lastEnd = NO_WORD;
        for (filter.reset(text); filter.next();) {
            if (filter.begin() >= lastEnd) {
                System.out.println();
                lastEnd = filter.end();
            }
            System.out.println(filter + " <-- " + getDF(filter.hash())  + " " + filter.begin() + " " + filter.end() + " " + filter.source().subSequence(filter.begin(), filter.end()));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        while (true) {

            String word = br.readLine();

            int dfDE = getDf(word, filterDE);
            int dfEN = getDf(word, filterEN);

            int max = Math.max(dfEN, dfDE);

            String stemDE = filterDE.toString();
            String stemEN = filterEN.toString();

            if (stemDE.equals(stemEN)) {
                System.out.println("  egal   \t" + dfDE + "\t--> " + stemDE);
            } else {
                if (dfDE > max / 30) System.out.println("  deutsch \t" + dfDE + "\t--> " + stemDE);
                if (dfEN > max / 30) System.out.println("  englisch\t" + dfEN + "\t--> " + stemEN);
            }

            System.out.println();
        }

    }

}
