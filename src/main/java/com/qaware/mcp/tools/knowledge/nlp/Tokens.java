package com.qaware.mcp.tools.knowledge.nlp;

/**
 * The Tokens interface represents a stream of tokens derived from a character sequence.
 * <p>
 * It extends {@link CharSequence} and provides additional methods for tokenization,
 * including navigation, resetting, and accessing the source data.
 * </p>
 * <p>
 * This interface is inspired by the Lucene Processing Pipeline, focusing on efficiency
 * and minimizing garbage collection (GC) overhead. It is designed for high-performance
 * text processing tasks where resource management is critical.
 * </p>
 */
public interface Tokens extends CharSequence {

    /**
     * Returns the length of the current token.
     *
     * @return the length of the current token
     */
    @Override
    default int length() {
        return end() - begin();
    }


    /**
     * Returns the character at the specified index within the current token.
     *
     * @param index the index of the character to return
     * @return the character at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    default char charAt(int index) {
        return source().charAt(begin() + index);
    }


    /**
     * Returns a subsequence of the current token.
     *
     * @param start the starting index of the subsequence
     * @param end   the ending index (exclusive) of the subsequence
     * @return the specified subsequence as a {@link CharSequence}
     */
    @Override
    default CharSequence subSequence(int start, int end) {
        return Chars.subSequence(this, start, end);
    }


    /**
     * Computes a hash value for the current token.
     *
     * @return the hash value of the current token
     */
    default long hash() {
        return Chars.hash(this);
    }


    /**
     * Resets the token stream with a new character sequence.
     *
     * @param chars the new character sequence to tokenize
     * @return this Tokens instance for method chaining
     */
    Tokens reset(CharSequence chars);


    /**
     * Advances to the next token in the stream.
     *
     * @return {@code true} if a next token exists, {@code false} otherwise
     */
    boolean next();


    /**
     * Returns the original character sequence from which tokens are derived.
     *
     * @return the source character sequence
     */
    CharSequence source();


    /**
     * Returns the starting index of the current token in the source sequence.
     *
     * @return the starting index of the current token
     */
    int begin();


    /**
     * Returns the ending index (exclusive) of the current token in the source sequence.
     *
     * @return the ending index of the current token
     */
    int end();


    /**
     * Provides access to the internal character buffer for the current token.
     *
     * @return the internal character buffer
     */
    char[] buffer();

}
