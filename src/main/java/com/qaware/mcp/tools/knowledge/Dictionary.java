package com.qaware.mcp.tools.knowledge;

import java.util.Arrays;

import com.carrotsearch.hppc.LongIntHashMap;
import com.qaware.mcp.tools.knowledge.nlp.Chars;

public class Dictionary { // XXX die klasse kann weg, ist erstmal okay

    public static final int NOT_FOUND = -1;

    private char[] chars = {};

    private int[] offsets = { 0 };

    private int pos;

    private int size;

    private LongIntHashMap hashToId = new LongIntHashMap(1_000, 0.5f);


    public int size() {
        return size;
    }


    public int get(CharSequence chars) {
        return get(chars, 0, chars.length());
    }


    public int get(CharSequence chars, int start, int end) {
        return get(Chars.hash(chars, start, end));
    }


    public int get(char[] chars, int start, int end) {
        return get(Chars.hash(chars, start, end));
    }


    public int get(long hash) {
        return hashToId.getOrDefault(hash, NOT_FOUND);
    }


    public int add(char[] aChars, int start, int end) {
        return add(Chars.hash(aChars, start, end), aChars, start, end);
    }


    public String get(int index) {
        int start = offsets[index];
        return new String(chars, start, offsets[index + 1] - start);
    }


    private synchronized int add(long hash, char[] aChars, int start, int end) {
        int index = hashToId.indexOf(hash);
        if (index >= 0) {
            return hashToId.indexGet(index);
        }

        hashToId.indexInsert(index, hash, size);

        int oldPos = pos;

        pos += end - start;

        if (pos > chars.length) {
            chars = Arrays.copyOf(chars, pos * 3 / 2);
        }

        System.arraycopy(aChars, start, chars, oldPos, end - start);

        if (offsets.length == size + 1) {
            offsets = Arrays.copyOf(offsets, size * 3 / 2 + 4);
        }

        offsets[size + 1] = pos;

        return size++;
    }

}
