package com.qaware.mcp.tools.knowledge.nlp;

import java.util.Arrays;

// copied from org.apache.lucene.analysis.de.GermanNormalizationFilter

public class FilterGermanNormalization extends Filter {

    // FSM with 3 states:
    private static final int N = 0; /* ordinary state */
    private static final int V = 1; /* stops 'u' from entering umlaut state */
    private static final int U = 2; /* umlaut state, allows e-deletion */


    public FilterGermanNormalization(Tokens tokens) {
        super(tokens);
    }


    @Override
    protected void filter() {
        int state = N;

        for (int i = 0; i < length; i++) {
            char c = buffer[i];
            switch(c) {

                case 'a':
                case 'o':
                    state = U;
                    break;

                case 'u':
                    state = (state == N) ? U : V;
                    break;

                case 'e':
                    if (state == U) length = delete(buffer, i--, length);
                    state = V;
                    break;

                case 'i':
                case 'q':
                case 'y':
                    state = V;
                    break;

                case 'ä':
                    buffer[i] = 'a';
                    state = V;
                    break;

                case 'ö':
                    buffer[i] = 'o';
                    state = V;
                    break;

                case 'ü':
                    buffer[i] = 'u';
                    state = V;
                    break;

                case 'ß':
                    buffer[i++] = 's';
                    if (length == buffer.length) buffer = Arrays.copyOf(buffer, length * 3 / 2); //XXX potentiell blöd
                    if (i < length) System.arraycopy(buffer, i, buffer, i + 1, length - i);
                    buffer[i] = 's';
                    length++;
                    state = N;
                    break;

                default:
                    state = N;
            }
        }
    }

}
