package com.qaware.mcp.tools.knowledge.nlp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class CharsTest {

    private static final String STRING = "Hello";


    @Test
    void testSubSequence() {
        Tokens tokens = new TokenizerSimple().reset(" ABCDEFGH ");
        tokens.next();

        CharSequence cut = Chars.subSequence(tokens, 1, 7);
        assertEquals("BCDEFG", cut.toString());
        assertEquals(6,  cut.length());
        assertEquals('D',  cut.charAt(2));

        assertEquals("DE", cut.subSequence(2, 4).toString());

        assertSame(cut.toString(), cut.toString()); // caches
    }


    @Test
    void testSubSequenceString() {
        assertEquals("el", Chars.subSequence(STRING, 1, 3).toString());
    }


    @Test
    void testSubSequenceSame() {
        assertSame(STRING, Chars.subSequence(STRING, 0, STRING.length()));
    }

}
