package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.tools.knowledge.quantization.CountMinSketch;
import com.qaware.mcp.tools.knowledge.quantization.Quantizer;
import com.qaware.mcp.tools.knowledge.quantization.QuantizerTable;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.German2Stemmer;

import com.carrotsearch.hppc.LongHashSet;
import com.carrotsearch.hppc.LongSet;
import com.qaware.mcp.tools.knowledge.nlp.*;

/*
jmcp com.qaware.mcp.tools.knowledge.quantization.Main2
*/
public enum Linguistic {

    ;


    private static final LongSet stopWordHashes = new LongHashSet();

    private static final Quantizer quantizer = new QuantizerTable(QuantizerTable.exponential(256, 1.06f));

    private static final CountMinSketch dfMinSketch;


    static {
        for (long hash : (long[]) Serialization.readObject("stopword-hashes.dat")) stopWordHashes.add(hash);

        dfMinSketch = new CountMinSketch(4, Serialization.readObject("df-de-en-freq-count-min-sketch.dat"));
    }


    public static void main(String[] args) throws Exception {

//        Filter filterEN =
//            new FilterStemmerSnowball(
//                new FilterStopWords(
//                    new FilterGermanNormalization(
//                        new FilterEnglishPossessive(
//                            new FilterToLower(
//                                new TokenizerSimple()))),
//                    stopWordHashes),
//                new EnglishStemmer());
//
//
//        Filter filterDE =
//            new FilterStemmerSnowball(
//                new FilterStopWords(
//                    new FilterGermanNormalization(
//                        new FilterEnglishPossessive(
//                            new FilterToLower(
//                                new TokenizerSimple()))),
//                    stopWordHashes),
//                new German2Stemmer());


        Filter filter = newFilter();



        int lastEnd = -1;
        for (filter.reset("!!!  #Running# informationEN   AutomatioN   THE ihr das la grünste ruNNing going hellen inFOrmation hellste der letzte existS"); filter.next();) {
            if (filter.begin() >= lastEnd) {
                System.out.println();
                lastEnd = filter.end();
            }
            System.out.println(" --> " + filter + " " + quantizer.get(dfMinSketch.getRaw(filter.hash()))  + " " + filter.begin() + " " + filter.end() + " " + filter.source().subSequence(filter.begin(), filter.end()));
        }






//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
//
//        while (true) {
//
//            String word = br.readLine();
//
//            System.out.println(dfMinSketch.getScore(getHash(filterDE, word), quantizer));
//            System.out.println(dfMinSketch.getScore(getHash(filterEN, word), quantizer));
//
//            System.out.println();
//        }


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

}
