package com.qaware.mcp.tools.knowledge.nlp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class BytesDecoderTest {

    private static final String TEST_STRING = "Hällo";

    private final BytesDecoder bytesDecoder = new BytesDecoder();


    @Test
    void test() {
        testIntern();
        testIntern(); // test reuse
    }


    @Test
    void testError() {
        RuntimeException rte = assertThrows(RuntimeException.class, () -> bytesDecoder.reset((byte) -127));

        assertEquals("java.nio.charset.MalformedInputException: Input length = 1", rte.getCause().toString());
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
