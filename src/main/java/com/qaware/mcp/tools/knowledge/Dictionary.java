package com.qaware.mcp.tools.knowledge;

import java.util.Arrays;

import com.carrotsearch.hppc.LongIntHashMap;
import com.qaware.mcp.tools.knowledge.nlp.Chars;

/**
 * A compact and efficient dictionary for mapping character sequences (words) to integer IDs and back.
 * <p>
 * Internally, words are stored in a single char array, and their start positions are tracked in an offsets array.
 * Lookup and insertion are performed using a 64-bit hash of the word. The mapping from hash to ID is managed by a LongIntHashMap.
 * <p>
 * <b>Hash Collisions:</b> This implementation assumes that hash collisions are extremely rare when using a high-quality 64-bit hash function.
 * In practice, for realistic dictionary sizes (up to hundreds of millions of entries), the probability of a collision is negligible (see Birthday Paradox).
 * <p>
 * <b>Thread Safety:</b> Only the add(long, char[], int, int) method is synchronized. Other methods are not thread-safe.
 * <p>
 * This class is optimized for performance and memory usage, not for persistence or serialization.
 */
public class Dictionary {

    /** Constant returned when a word is not found in the dictionary. */
    public static final int NOT_FOUND = -1;

    /** Stores all characters of all words in a single array. */
    private char[] chars = {};

    /** Stores the start offsets of each word in the chars array. */
    private int[] offsets = { 0 };

    /** Current position in the chars array (end of the last word). */
    private int pos;

    /** Number of words in the dictionary. */
    private int size;

    /** Maps 64-bit hash values to word IDs (indices). */
    private LongIntHashMap hashToId = new LongIntHashMap(1_000, 0.5f);


    /** Returns the number of words in the dictionary. */
    public int size() {
        return size;
    }


    /** Looks up the ID */
    public int get(CharSequence chars) {
        return get(chars, 0, chars.length());
    }


    /** Looks up the ID */
    public int get(CharSequence chars, int start, int end) {
        return get(Chars.hash(chars, start, end));
    }


    /** Looks up the ID */
    public int get(char[] chars, int start, int end) {
        return get(Chars.hash(chars, start, end));
    }


    /** Looks up the ID */
    public int get(long hash) {
        return hashToId.getOrDefault(hash, NOT_FOUND);
    }


    /**
     * Adds a new word to the dictionary if not already present.
     * @param chars the char array containing the word
     * @param start start index (inclusive)
     * @param end end index (exclusive)
     * @return the ID of the word (existing or new)
     */
    public int add(char[] chars, int start, int end) {
        return add(Chars.hash(chars, start, end), chars, start, end);
    }


    /**
     * Returns the word at the given index as a String.
     * @param index the word ID
     * @return the word, or null if index is out of bounds
     */
    public String get(int index) {
        if (index < 0 || index >= size) return null;
        int start = offsets[index];
        return new String(chars, start, offsets[index + 1] - start);
    }


    /** Adds a new word to the dictionary using its hash, if not already present. This method is synchronized for thread safety. */
    private synchronized int add(long hash, char[] aChars, int start, int end) {
        int index = hashToId.indexOf(hash);
        if (index >= 0) return hashToId.indexGet(index);

        hashToId.indexInsert(index, hash, size);

        int oldPos = pos;
        pos += end - start;
        if (pos > chars.length) chars = Arrays.copyOf(chars, pos * 3 / 2);
        System.arraycopy(aChars, start, chars, oldPos, end - start);

        if (offsets.length == size + 1) offsets = Arrays.copyOf(offsets, size * 3 / 2 + 4);
        offsets[size + 1] = pos;

        return size++;
    }

}
