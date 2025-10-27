package com.qaware.mcp.tools.knowledge.quantization;

import java.util.Arrays;

/**
 * The QuantizerTable class implements the Quantizer interface and provides functionality for quantization
 * based on a sorted array of floats. It supports binary search for efficient quantization and includes
 * utility methods for generating exponential quantization tables.
 */
public class QuantizerTable implements Quantizer {

    /**
     * The sorted array of floats used for quantization.
     */
    private final float[] floats;


    /**
     * Constructs a QuantizerTable with the given array of floats. The array is sorted in ascending order.
     *
     * @param aFloats the array of floats to be used for quantization
     */
    public QuantizerTable(float... aFloats) {
        Arrays.sort(aFloats);

        floats = aFloats;
    }


    /**
     * Generates an exponential quantization table.
     *
     * @param size   the number of elements in the table
     * @param factor the exponential growth factor
     * @return an array of floats representing the exponential quantization table
     */
    public static float[] exponential(int size, float factor) {
        float[] floats = new float[size];

        double value = 1;
        for (int i = 1; i < size; i++) {
            value = Math.max(i, Math.floor(value));
            floats[i] = (float) value;
            value *= factor;
        }

        return floats;
    }


    /**
     * Retrieves the float value at the specified index.
     *
     * @param index the index of the value to retrieve
     * @return the float value at the specified index
     */
    @Override
    public float get(int index) {
        return floats[index];
    }


    /**
     * Quantizes the given value to the closest index in the sorted array of floats.
     *
     * @param value the value to quantize
     * @return the index of the closest quantized value
     */
    @Override
    public int quantize(float value) {
        int low  = 0;
        int high = size() - 1;

        while (low < high) {
            int mid = (low + high) >>> 1;

            if (value > get(mid)) low  = mid + 1;
            else                  high = mid;
        }

        if (low == 0) return 0;

        return low - (get(low) - value > value - get(low - 1) ? 1 : 0);
    }


    /**
     * Returns the size of the quantization table.
     *
     * @return the number of elements in the quantization table
     */
    @Override
    public int size() {
        return floats.length;
    }

}
