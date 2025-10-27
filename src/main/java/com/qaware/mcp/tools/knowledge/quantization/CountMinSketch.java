package com.qaware.mcp.tools.knowledge.quantization;

import java.util.Arrays;

/**
 * Count-Min Sketch for frequency estimation using conservative updates.
 *
 * <p>Tracks quantized frequency estimates (8-bit values) with multiple hash functions to minimize collision errors.
 * Uses MAX during updates (conservative) and MIN during queries with uncertainty correction for improved accuracy on
 * rare items.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * CountMinSketch sketch = new CountMinSketch(4, 10000);
 * byte quantized = quantizer.quantize(frequency);
 * sketch.update(hash, quantized);
 * float estimate = sketch.getScore(hash, quantizer);
 * }</pre>
 *
 * <p><b>Thread Safety:</b> Multiple threads may call getScore() concurrently. However, update() requires external
 * synchronization and must not run concurrently with any other operation.
 *
 * @see Quantizer
 */
public class CountMinSketch {

    private static final long MURMUR_MIXING_CONSTANT = 0xc6a4a7935bd1e995L;

    private static final int UNCERTAINTY_CORRECTION_SHIFT = 2;


    /** Number of hash functions used for error reduction */
    private final int samples;

    /** Sketch storage for quantized frequency values (8-bit) */
    private final byte[] values;


    /** Gets the internal values */
    public byte[] getValues() {
        return values;
    }


    /**
     * Creates a new sketch initialized to minimum values.
     *
     * @param aSamples number of hash functions (typically 3-5)
     * @param size sketch width (array size)
     *
     * Using a prime for size is highly recommended.
     */
    public CountMinSketch(int aSamples, int size) {
        samples = aSamples;
        values  = new byte[size];
        Arrays.fill(values, Byte.MIN_VALUE);
    }


    /**
     * Creates a sketch from existing data.
     *
     * @param aSamples number of hash functions
     * @param aValues pre-initialized sketch values
     */
    public CountMinSketch(int aSamples, byte... aValues) {
        samples = aSamples;
        values  = aValues;
    }


    /**
     * Updates the sketch with a quantized frequency value (conservative update).
     *
     * <p>Uses MAX to retain highest observed value across hash collisions.
     *
     * @param hash item hash (will be mixed for multiple positions)
     * @param value quantized frequency (8-bit, from {@link Quantizer#quantize})
     */
    public void update(long hash, byte value) {
        for (int i = 0; i < samples; i++) {
            int index = getIndex(hash);
            values[index] = (byte) Math.max(values[index], value); // CountMinSketch conservative update
            hash = mix(hash);
        }
    }


    /**
     * Estimates frequency for a given hash with uncertainty correction.
     *
     * <p>Returns the minimum quantized value across hash functions. If the minimum
     * is not confirmed by a second sample (likely collision), applies correction
     * by dividing the quantizer index by 4 to reduce overestimation.
     *
     * @param hash item hash to query
     * @param quantizer dequantizer for 8-bit values (size ≤ 256)
     * @return estimated frequency value
     */
    @SuppressWarnings("java:S4274")
    public float getScore(long hash, Quantizer quantizer) {
        assert quantizer.size() <= 256;

        return quantizer.get(getRaw(hash));
    }


    /** Returns the raw quantized value */
    public int getRaw(long hash) {
        byte min  = Byte.MAX_VALUE;
        byte min2 = Byte.MAX_VALUE;

        for (int i = 0; i < samples; i++) {
            int index = getIndex(hash);

            byte value = values[index];

            if (value < min) {
                min2 = min;
                min  = value;
            } else {
                if (value < min2) min2 = value;
            }

            hash = mix(hash);
        }

        int index = min + 128;

        /*
         * This is a trick I invented myself. It more than halves the relative errors on rare values. It is basically an
         * uncertainty correction: If the minimum value is not confirmed by a second-lowest value (min != min2),
         * the estimate is likely inflated due to  hash collisions. Empirical testing shows dividing the quantizer
         * index by 4 (which is the same as shifting by 2) provides the best accuracy across various parameter
         * combinations. This has been measured on the Wikipedia words for German and English (measured relative error
         * drops from 3.27 to 1.28 which is woho!!!). Actually, I should write a small paper about this.
         */
        return min == min2 ? index : index >>> UNCERTAINTY_CORRECTION_SHIFT;
    }


    /** Maps hash to sketch array index. */
    private int getIndex(long hash) {
        return (int) ((hash >>> 1) % values.length);
    }


    /** Mixes hash for next sample using MurmurHash constant. */
    private static long mix(long hash) {
        return Long.reverseBytes(hash * MURMUR_MIXING_CONSTANT);
    }

}
