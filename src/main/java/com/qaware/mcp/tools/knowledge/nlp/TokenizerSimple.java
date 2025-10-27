package com.qaware.mcp.tools.knowledge.nlp;

/**
 * A simple tokenizer implementation that splits a given CharSequence into tokens.
 * Tokens are sequences of letters or digits, separated by whitespace or hyphens.
 */
public class TokenizerSimple implements Tokens {

    private static final char[] EMPTY_CHAR_ARRAY = {};


    private CharSequence chars = "";

    private int next;

    private int length;

    private int begin;
    private int end;


    private char[] buffer = EMPTY_CHAR_ARRAY;

    /**
     * Resets the tokenizer with a new input CharSequence.
     *
     * @param aChars the input CharSequence to tokenize
     * @return the current instance of TokenizerSimple
     */
    @Override
    public TokenizerSimple reset(CharSequence aChars) {
        next   = 0;
        length = aChars.length();
        chars  = aChars;
        return this;
    }


    @Override
    public String toString() {
        return Chars.toString(this, 0, length());
    }


    @Override
    public CharSequence source() {
        return chars;
    }

    @Override
    public int begin() {
        return begin;
    }


    @Override
    public int end() {
        return end;
    }


    @Override
    public boolean next() {
        for (; next < length; next++) {
            if (Character.isLetterOrDigit(chars.charAt(next))) {
                scanToken();
                return true;
            }
        }

        return false;
    }


    /**
     * Scans the input to identify the next token.
     * Updates the begin and end indices to mark the token boundaries.
     */
    private void scanToken() {
        begin = next;
        end   = next;

        while (++next < length) {
            char chr = chars.charAt(next);

            if (Character.isLetterOrDigit(chr)) {
                end = next;
            } else if (Character.isWhitespace(chr) || chr == '-') {
                break;
            }
        }

        end++;
    }


    /**
     * Copies the current token into a char array buffer.
     *
     * @return the char array containing the current token
     */
    @Override
    public char[] buffer() {
        buffer = Chars.copy(this, buffer);
        return buffer;
    }

}
