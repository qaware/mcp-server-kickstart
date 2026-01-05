package com.qaware.mcp.tools.knowledge.nlp;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Objects;

/**
 * A reusable, NON-thread-safe decoder for converting byte arrays into character sequences.
 * <p>
 * This class is designed for efficiency and is intended to be reused within a single thread.
 * It uses a {@link CharsetDecoder} to decode byte arrays into characters and provides direct
 * access to the internal character buffer for zero-copy operations.
 * </p>
 * <p><b>Note:</b> This class is not thread-safe. Each thread must use its own instance.</p>
 */
public class BytesDecoder implements CharSequence {

    /**
     * The factor used to determine when to reallocate the internal buffer.
     */
    private static final int REALLOCATION_THRESHOLD_FACTOR = 20;

    private CharBuffer charBuffer;

    private int length;

    private char[] chars;

    private final CharsetDecoder charsetDecoder;


    /**
     * Constructs a new BytesDecoder with the default UTF-8 charset.
     */
    public BytesDecoder() {
        this(StandardCharsets.UTF_8);
    }


    /**
     * Constructs a new BytesDecoder with the specified charset.
     *
     * @param charset the charset to use for decoding
     */
    public BytesDecoder(Charset charset) {
        charsetDecoder = charset.newDecoder();
    }


    /**
     * Resets the decoder with the given byte array.
     *
     * @param bytes the byte array to decode
     * @return this BytesDecoder instance for method chaining
     */
    public BytesDecoder reset(byte... bytes) {
        return reset(bytes, 0, bytes.length);
    }


    /**
     * Resets the decoder with a subset of the given byte array.
     *
     * @param bytes the byte array to decode
     * @param begin the starting index of the subset
     * @param end   the ending index (exclusive) of the subset
     * @return this BytesDecoder instance for method chaining
     */
    @SuppressWarnings("java:S2259")
    public BytesDecoder reset(byte[] bytes, int begin, int end) {
        int requiredSize = (int) ((end - begin) * charsetDecoder.maxCharsPerByte() + 1);

        int capacity = charBuffer == null ? -1 : charBuffer.capacity();
        if (capacity < requiredSize || capacity > requiredSize * REALLOCATION_THRESHOLD_FACTOR) { // if too small or really oversized...
            charBuffer = CharBuffer.allocate(requiredSize | 31); // over allocate a bit for small sizes
        }

        charBuffer.clear(); // charBuffer can not be null at this point

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, begin, end - begin);
        charsetDecoder.reset();

        length = 0; // make length invalid if case checks throws exception, we can keep chars as it is

        check(charsetDecoder.decode(byteBuffer, charBuffer, true));
        check(charsetDecoder.flush(charBuffer));

        charBuffer.flip();

        length = charBuffer.remaining();
        chars  = charBuffer.array();

        return this;
    }


    /**
     * Returns the length of the decoded character sequence.
     *
     * @return the number of characters in the decoded sequence
     */
    @Override
    public int length() {
        return length;
    }


    /**
     * Provides direct access to the internal character buffer for zero-copy operations.
     * <p><b>Warning:</b> Do not retain references beyond the next {@link #reset(byte[])} call.
     * The array content and reference may change on reset.
     *
     * @return the internal character buffer (size >= {@link #length()})
     */
    public char[] buffer() {
        return chars;
    }


    @Override
    public char charAt(int index) {
        Objects.checkIndex(index, length);
        return chars[index];
    }


    @Override
    public String toString() {
        return new String(chars, 0, length);
    }


    @Override
    public CharSequence subSequence(int start, int end) {
        return Chars.subSequence(this, start, end);
    }


    /**
     * Checks the result of a decoding operation and throws an exception if an error occurred.
     *
     * @param coderResult the result of the decoding operation
     * @throws UncheckedIOException if a decoding error occurred
     */
    @SuppressWarnings("java:S112")
    private static void check(CoderResult coderResult) {
        if (coderResult.isError()) {
            try {
                coderResult.throwException();
            } catch (CharacterCodingException cce) {
                throw new UncheckedIOException(cce);
            }
        }
    }

}
