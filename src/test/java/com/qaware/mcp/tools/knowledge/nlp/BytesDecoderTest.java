package com.qaware.mcp.tools.knowledge.nlp;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BytesDecoderTest {

    private static final String TEST_STRING = "Hällo";

    private final BytesDecoder bytesDecoder = new BytesDecoder();


    @Test
    void test() {
        testIntern();
        testIntern(); // test reuse
    }


    @Test
    void testIllegalBytes() {
        bytesDecoder.reset((byte) 'h', (byte) -127, (byte) 'i', (byte) -127);

        assertEquals("h�i�", bytesDecoder.toString());
    }

    private void testIntern() {
        bytesDecoder.reset(TEST_STRING.getBytes(StandardCharsets.UTF_8));

        assertEquals('ä', bytesDecoder.charAt(1));

        assertEquals(TEST_STRING, bytesDecoder.toString());
        assertEquals(TEST_STRING, new String(bytesDecoder.buffer(), 0, bytesDecoder.length()));

        CharSequence subSequence = bytesDecoder.subSequence(1, 3);
        assertFalse(subSequence instanceof String);
        assertEquals("äl", subSequence.toString());

        assertThrows(IndexOutOfBoundsException.class, () -> bytesDecoder.charAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> bytesDecoder.charAt(5));
    }

}
