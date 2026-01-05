package com.qaware.mcp.tools.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DictionaryTest {

    @Test
    void test() {
        Dictionary dictionary = new Dictionary();

        assertNull(dictionary.get(-1));
        assertNull(dictionary.get(0));

        char[] chars = "Hello Hello".toCharArray();

        assertEquals(0, dictionary.add(chars, 0, 5));
        assertEquals(0, dictionary.add(chars, 6, 11));

        assertEquals("Hello", dictionary.get(0));

        assertEquals(0, dictionary.get("Hello"));
        assertEquals(0, dictionary.get(chars, 6, 11));

        assertEquals(1, dictionary.add(chars, 0, 0));

        assertEquals("", dictionary.get(1));

        assertEquals(2, dictionary.size());
    }

}
