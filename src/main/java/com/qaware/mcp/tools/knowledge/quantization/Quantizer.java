package com.qaware.mcp.tools.knowledge.quantization;

/**
 * Bidirectional mapping between continuous float values and discrete integer indices.
 *
 * <p>Maps floats to quantization levels in range {@code [0, size()-1]} with information loss.
 * Useful for compressing floating-point data in probabilistic data structures.
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * Quantizer q = ...
 * int index = q.quantize(42.7f);      // encode: float -> int
 * float value = q.get(index);          // decode: int -> float (~42.7f)
 * }</pre>
 *
 * <p><b>Contract:</b>
 * <ul>
 *   <li>{@link #quantize(float)} always returns values in {@code [0, size() - 1]}</li>
 *   <li>{@link #get(int)} accepts indices in {@code [0, size() - 1]}</li>
 *   <li>{@link #size()} returns a constant positive value</li>
 * </ul>
 *
 * <p>Implementations should be immutable and thread-safe.
 *
 * The quantization values are ordered that means:
 * <pre>get(i) > get(i - 1)</pre>
 */
public interface Quantizer {

    /**
     * Returns the number of discrete quantization levels.
     *
     * @return number of levels, must be positive and constant
     */
    int size(); // range of quantization values

    /**
     * Encodes a float value to its nearest quantization index.
     *
     * @param value the float to quantize
     * @return quantization index in {@code [0, size()-1]}
     *
     * @implNote Handle special cases (NaN, infinity) by clamping to valid range.
     */
    int quantize(float value);


    /**
     * Decodes a quantization index to its representative float value.
     *
     * @param index quantization level in {@code [0, size()-1]}
     * @return the float value representing this level
     * @throws IndexOutOfBoundsException if index out of range (implementation-dependent)
     */
    float get(int index);

}
