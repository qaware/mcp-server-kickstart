package com.qaware.mcp.tools.knowledge.nlp;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code FilterCombine} class extends the {@link Filter} class and allows combining multiple filters.
 * It iterates over a list of delegate filters and applies their filtering logic sequentially.
 *
 * <p><b>Internal Logic:</b></p>
 * <ul>
 *     <li>
 *         <b>Delegates:</b> The class maintains a list of delegate filters ({@code delegates}).
 *         These delegates are processed sequentially to find the next valid token.
 *     </li>
 *     <li>
 *         <b>Index Management:</b> The {@code index} variable tracks the current delegate being processed.
 *         The {@code INIT} constant represents the initial state before any delegate is processed.
 *     </li>
 *     <li>
 *         <b>Buffer Management:</b> The class uses a copy buffer ({@code copyBuffer}) to store the current state
 *         of the buffer. This ensures that the state can be restored if a delegate does not produce a valid result.
 *     </li>
 *     <li>
 *         <b>Recursive Processing:</b> The {@code next()} method is the core of the filtering logic. It recursively
 *         iterates over the delegates until a valid token is found or all delegates are exhausted.
 *     </li>
 *     <li>
 *         <b>Flags:</b> The class uses flags ({@code isFirst} and {@code isFromDelegate}) to manage the state of
 *         processing. These flags ensure that the logic correctly handles transitions between delegates.
 *     </li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <ol>
 *     <li>Create an instance of {@code FilterCombine} with a base {@link Tokens} object.</li>
 *     <li>Add additional filters using the {@link #combine(Filter)} method.</li>
 *     <li>Call {@link #next()} to iterate through the combined filters and retrieve valid tokens.</li>
 * </ol>
 *
 * <p><b>Example:</b></p>
 * <pre>
 *     Tokens baseTokens = new BaseTokens();
 *     FilterCombine filterCombine = new FilterCombine(baseTokens);
 *     filterCombine.combine(new CustomFilter1());
 *     filterCombine.combine(new CustomFilter2());
 *
 *     while (filterCombine.next()) {
 *         System.out.println(filterCombine);
 *     }
 * </pre>
 */
public class FilterCombine extends Filter {

    private static final int INIT = -1;

    private int index;

    private boolean isFirst;
    private boolean isFromDelegate;

    private List<Tokens> delegates = new ArrayList<>();

    private int copyLength;
    private char[] copyBuffer = {};


    public FilterCombine(Tokens tokens) {
        super(tokens);
    }


    /**
     * Resets the filter and all its delegates with the given character sequence.
     *
     * @param chars the character sequence to reset the filter with.
     * @return the current {@code FilterCombine} instance.
     */
    @Override
    public Filter reset(CharSequence chars) {
        if (chars == this) return this;

        setIndex(INIT);
        isFromDelegate = false;

        delegates.forEach(tokens -> reset(this));

        return super.reset(chars);
    }


    /**
     * Advances to the next valid token by iterating over the delegates.
     *
     * @return {@code true} if a valid token is found, {@code false} otherwise.
     */
    @Override
    public boolean next() {
        while (true) {

            if (index == INIT) {
                if (!super.next()) return false;

                setIndex(0);
            }

            if (isFromDelegate) {
                if (isFirst) {
                    isFirst = false;
                    return true;
                }

                return false;
            }

            Tokens delegate = delegates.get(index);

            isFromDelegate = true;
            boolean next = delegate.next();
            isFromDelegate = false;

            if (next) {
                accept(delegate);
                return true;
            }

            setIndex(index + 1);

            if (index == delegates.size()) {
                index = INIT;
            } else {
                length = copyLength;
                copy(copyBuffer, buffer);
            }
        }
    }


    public void combine(Filter filter) {
        delegates.add(filter);
    }


    /**
     * Stores the current state of the buffer for later restoration.
     */
    @Override
    protected void filter() {
        if (length > copyBuffer.length) copyBuffer = new char[length * 3 / 2];

        copyLength = length;
        copy(buffer, copyBuffer);
    }


    /**
     * Sets the index of the current delegate and marks it as the first processing attempt.
     *
     * @param newIndex the new index to set.
     */
    private void setIndex(int newIndex) {
        index   = newIndex;
        isFirst = true;
    }

}
