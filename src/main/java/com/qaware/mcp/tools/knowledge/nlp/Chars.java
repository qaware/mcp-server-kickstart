package com.qaware.mcp.tools.knowledge.nlp;

/**
 * Utility class for working with CharSequence and char arrays.
 * Provides methods for subsequences, copying, hashing, and case conversion.
 */
public enum Chars {

    ;


    private static final long MURMUR_MIXING_CONSTANT = 0xc6a4a7935bd1e995L;


    /**
     * Returns a subsequence of the given CharSequence.
     *
     * @param charSequence the original CharSequence
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return a new CharSequence representing the subsequence
     */
    public static CharSequence subSequence(CharSequence charSequence, int start, int end) {
        if (start == 0 && end == charSequence.length()) return charSequence;

        return new CharSequence() {

            private volatile String cachedString;


            @Override
            public int length() {
                return end - start;
            }


            @Override
            public char charAt(int index) {
                return charSequence.charAt(index + start);
            }


            @Override
            public CharSequence subSequence(int startSub, int endSub) {
                return Chars.subSequence(charSequence, start + startSub, start + endSub);
            }


            @Override
            public String toString() {
                if (cachedString == null) cachedString = Chars.toString(charSequence, start, end); // race condition possible but acceptable

                return cachedString;
            }

        };
    }


    /**
     * Converts a subsequence of the given CharSequence to a String.
     *
     * @param charSequence the original CharSequence
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return the resulting String
     */
    public static String toString(CharSequence charSequence, int start, int end) {
        if (charSequence instanceof String string) return string.substring(start, end);

        char[] chars = new char[end - start];
        for (int i = 0; i < chars.length; i++) chars[i] = charSequence.charAt(start + i);

        return new String(chars);
    }


    /**
     * Copies the content of a CharSequence into a char array.
     *
     * @param chars the source CharSequence
     * @param target the target char array
     * @return the target char array, resized if necessary
     */
    public static char[] copy(CharSequence chars, char[] target) {
        int length = chars.length();

        if (target.length < length) target = new char[length * 3 / 2 + 8]; // conservative new size

        copy(chars, 0, length, target, 0);

        return target; // return potentially resized array
    }


    /**
     * Copies a portion of a CharSequence into a char array.
     *
     * @param chars the source CharSequence
     * @param begin the start index (inclusive)
     * @param end the end index (exclusive)
     * @param target the target char array
     * @param offset the starting position in the target array
     */
    public static void copy(CharSequence chars, int begin, int end, char[] target, int offset) {
        int length = end - begin;

        for (int i = 0; i < length; i++) target[i + offset] = chars.charAt(i + begin);
    }


    /**
     * Computes a hash value for the given CharSequence.
     *
     * @param chars the CharSequence to hash
     * @return the hash value
     */
    public static long hash(CharSequence chars) {
        return hash(chars, 0, chars.length());
    }


    /**
     * Computes a hash value for a portion of the given CharSequence.
     *
     * @param chars the CharSequence to hash
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return the hash value
     */
    public static long hash(CharSequence chars, int start, int end) {
        long hash = 0;

        for (int pos = start; pos < end; pos++) hash = mix(hash + chars.charAt(pos));

        return mix(hash + end - start);
    }


    /**
     * Computes a hash value for a portion of the given char array.
     *
     * @param chars the char array to hash
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return the hash value
     */
    @SuppressWarnings("java:S2184")
    public static long hash(char[] chars, int start, int end) {
        long hash = 0;

        for (int pos = start; pos < end; pos++) hash = mix(hash + chars[pos]);

        return mix(hash + end - start);
    }


    /**
     * Converts characters in the given range of a char array to lowercase.
     *
     * @param chars the char array to modify
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return the end of the modified array
     */
    public static int toLower(char[] chars, int start, int end) {
        int writePos = start;
        int readPos  = start;

        while (readPos < end) {
            int codePoint      = Character.codePointAt(chars, readPos, end);
            int lowerCodePoint = Character.toLowerCase(codePoint);

            writePos += Character.toChars(lowerCodePoint, chars, writePos);
            readPos  += Character.charCount(codePoint);
        }

        return writePos;
    }


    private static long mix(long hash) { // XXX auslagern?
        return Long.reverseBytes(hash * MURMUR_MIXING_CONSTANT);
    }

}
