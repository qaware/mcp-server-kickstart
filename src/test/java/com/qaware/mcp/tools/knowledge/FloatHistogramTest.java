package com.qaware.mcp.tools.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloatHistogramTest {

    @Test
    public void test() {
        FloatHistogram histogram = new FloatHistogram();
        for (int i = 0; i < 100; i++) histogram.increment(0.1f * (i % 17));

        assertEquals(1.405f, histogram.getThreshold(10));

        assertEquals(0f, histogram.getThreshold(1000));
    }

}
