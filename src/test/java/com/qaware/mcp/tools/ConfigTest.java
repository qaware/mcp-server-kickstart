package com.qaware.mcp.tools;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConfigTest {

    private static final String FALLBACK = "fallback";

    @Test
    void reset_shouldPickUpSystemProperties() {
        System.setProperty(" MAX_Co NT.-ent  ", "2000");

        Config.reset();

        assertEquals(2000, Config.getInt(" max-._Content ", -1)); // from sys prop
        assertEquals(10, Config.getInt(null, 10)); // fallback

        assertEquals(FALLBACK, Config.get("*unknown*", FALLBACK)); // from env
        assertNotNull(Config.get("PATH", FALLBACK)); // from env
    }

}
