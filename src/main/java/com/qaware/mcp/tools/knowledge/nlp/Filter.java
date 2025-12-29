package com.qaware.mcp.tools.knowledge.nlp;

/**
 * The Filter class is an abstract implementation of the {@link Tokens} interface, designed to process
 * and transform token streams. It acts as a wrapper around another {@link Tokens} instance, allowing
 * for the application of filtering logic to the token stream.
 * <p>
 * This class is inspired by the Lucene Processing Pipeline and focuses on efficiency and minimal
 * garbage collection (GC) overhead. Filters are designed to be reusable within a single thread.
 * </p>
 */
public abstract class Filter implements Tokens {

    /** The root {@link Tokens} instance, representing the original source of tokens. */
    private final Tokens root;

    /** The parent {@link Tokens} instance, representing the immediate upstream token source.*/
    protected final Tokens parent;


    /** The internal buffer holding the current token's characters. */
    protected char[] buffer;

    /** The length of the current token in the buffer. */
    protected int length;


    /**
     * Constructs a new Filter wrapping the given {@link Tokens} instance.
     *
     * @param tokens the {@link Tokens} instance to wrap
     */
    Filter(Tokens tokens) {
        parent = tokens;

        if (tokens instanceof Filter filter) tokens = filter.root;

        root = tokens;
    }


    /**
     * Applies the filtering logic to the current token. This method is intended to be overridden
     * by subclasses to implement specific filtering behavior.
     */
    protected void filter() {
        // to override
    }


    @Override
    public final String toString() {
        return Chars.toString(this, 0, length());
    }


    @Override
    public Filter reset(CharSequence chars) {
        parent.reset(chars);
        return this;
    }


    /**
     * Returns the original character sequence from which tokens are derived.
     *
     * @return the source character sequence
     */
    @Override
    public final CharSequence source() {
        return root.source();
    }


    /**
     * Advances to the next token in the stream, applying the filter logic.
     *
     * @return {@code true} if a next token exists, {@code false} otherwise
     */
    @Override
    public boolean next() {
        if (! parent.next()) return false;

        accept(parent);

        filter();

        return true;
    }


    /**
     * Returns the starting index of the current token in the source sequence.
     *
     * @return the starting index of the current token
     */
    @Override
    public int begin() {
        return root.begin();
    }


    /**
     * Returns the ending index (exclusive) of the current token in the source sequence.
     *
     * @return the ending index of the current token
     */
    @Override
    public int end() {
        return root.end();
    }


    /**
     * Provides access to the internal character buffer for the current token.
     *
     * @return the internal character buffer
     */
    @Override
    public final char[] buffer() {
        return buffer;
    }


    /**
     * Returns the length of the current token in the buffer.
     *
     * @return the length of the current token
     */
    @Override
    public final int length() {
        return length;
    }


    /**
     * Returns the character at the specified index within the current token.
     *
     * @param index the index of the character to return
     * @return the character at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public final char charAt(int index) {
        return buffer[index];
    }


    /**
     * Computes a hash value for the current token.
     *
     * @return the hash value of the current token
     */
    @Override
    public long hash() {
        return Chars.hash(buffer, 0, length);
    }


    /**
     * Accepts the current token from the parent {@link Tokens} instance, copying its buffer and length.
     *
     * @param tokens the parent {@link Tokens} instance
     */
    protected final void accept(Tokens tokens) {
        buffer = tokens.buffer();
        length = tokens.length();
    }


    /**
     * Copies the content of the source buffer into the target buffer.
     *
     * @param source the source buffer
     * @param target the target buffer
     */
    protected final void copy(char[] source, char[] target) {
        System.arraycopy(source, 0, target, 0, length);
    }


    /**
     * Deletes a character at the specified position in the buffer.
     *
     * @param chars the character buffer
     * @param pos   the position of the character to delete
     * @param len   the current length of the buffer
     * @return the new length of the buffer after deletion
     */
    protected static int delete(char[] chars, int pos, int len) {
        assert pos < len;

        if (pos < len - 1) { // don't arraycopy if asked to delete last character
            System.arraycopy(chars, pos + 1, chars, pos, len - pos - 1);
        }

        return len - 1;
    }

}
