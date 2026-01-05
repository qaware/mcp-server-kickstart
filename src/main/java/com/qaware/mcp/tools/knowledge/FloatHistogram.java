package com.qaware.mcp.tools.knowledge;

import java.util.Arrays;

/**
 * A histogram for floating-point values that counts occurrences in discrete buckets.
 * The resolution of the buckets is defined by the RESOLUTION constant.
 */
class FloatHistogram {

    private static final int RESOLUTION = 100;

    private int[] counts = {};


    /**
     * Increments the count for the bucket corresponding to the given float value.
     *
     * @param f the float value to be added to the histogram; values <= 0 are ignored
     */
    public void increment(float f) {
        if (f <= 0) return;

        int slot = Math.round(f * RESOLUTION);

        if (slot >= counts.length) counts = Arrays.copyOf(counts, slot * 3 / 2 + 4);

        counts[slot]++;
    }


    /**
     * Retrieves the threshold value for which the cumulative count of buckets
     * equals or exceeds the specified count.
     *
     * @param count the cumulative count threshold
     * @return the float value representing the threshold, or -1 if the count is not reached
     */
    public float getThreshold(int count) {
        for (int i = counts.length - 1; i >= 0; i--) {
            count -= counts[i];
            if (count < 0) return (i + 0.5f) * 1f / RESOLUTION;
        }

        return -1;
    }

}
