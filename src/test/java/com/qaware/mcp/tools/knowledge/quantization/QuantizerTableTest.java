package com.qaware.mcp.tools.knowledge.quantization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class QuantizerTableTest {

    @Test
    void test() {
        float[] floats = new float[256];
        for (int i = 0; i < floats.length; i++) floats[i] = i;

        Quantizer quantization = new QuantizerTable(floats);

        for (float f = -0.4f; f <= 255.4f; f += 0.001f) {
            int expected = Math.round(f);
            assertEquals(expected, quantization.quantize(f));
        }
    }


    @Test
    void testExponential() {
        assertEquals("[0.0, 1.0, 2.0, 3.0, 4.0, 6.0, 9.0, 13.0]", Arrays.toString(QuantizerTable.exponential(8, 1.5f)));
    }

}
