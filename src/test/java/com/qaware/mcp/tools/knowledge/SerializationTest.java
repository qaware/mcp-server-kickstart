package com.qaware.mcp.tools.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SerializationTest {

    private static final String TEST_STRING = "Test Test Test Test Test Test Test Test  Test Test Test Test Test Test";


    @Test
    void test() {
        byte[] bytes = Serialization.toBytes(TEST_STRING);
        String roundTrip = Serialization.fromBytes(bytes);

        assertTrue(bytes.length < TEST_STRING.length() / 2); // there is compression

        assertEquals(TEST_STRING, roundTrip);
    }

}
