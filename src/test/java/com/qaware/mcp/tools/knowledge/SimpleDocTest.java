package com.qaware.mcp.tools.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SimpleDocTest {

    @Test
    void test() {
        float[] scores = new float[20];

        scores[0] = 1;
        scores[19] = 1;
        SimpleDoc.smooth(scores);

        assertEquals(scores[0], scores[19], 0.0001);
    }


    @Test
    void test2() {
        float[] scores = new float[11];

        scores[5] = 1;
        SimpleDoc.smooth(scores);

        assertEquals(scores[0], scores[10], 0.0001);
    }

}
