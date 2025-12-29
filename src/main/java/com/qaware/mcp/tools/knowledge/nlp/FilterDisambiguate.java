package com.qaware.mcp.tools.knowledge.nlp;

import com.qaware.mcp.tools.knowledge.quantization.CountMinSketch;

import java.util.Arrays;

// hier muss ich nochmal drueber nachdenken, das funktioniert prima, ist aber echt fies!
public class FilterDisambiguate extends Filter {

    private static final int RELATIVE_RAW_THRESHOLD = 50;

    private static final char[] EMPTY_CHAR_ARRAY = {};

    private static final int INIT = -1;


    private final CountMinSketch minSketch;

    private int lastEnd = INIT;

    private int begin;
    private int end;

    private int emit;
    private int count;
    private char[] tokens = EMPTY_CHAR_ARRAY;
    private int[] offsets = new int[10];

    private int[] raws = new int[10];
    private int maxRaw;


    public FilterDisambiguate(Tokens tokens, CountMinSketch aMinSketch) {
        super(tokens);

        minSketch = aMinSketch;

        buffer = EMPTY_CHAR_ARRAY;
    }


    @Override
    public Filter reset(CharSequence chars) {
        lastEnd = INIT;
        emit    = 0;
        count   = 0;
        maxRaw  = 0;
        return super.reset(chars);
    }


    @Override
    public int begin() {
        return begin;
    }


    @Override
    public int end() {
        return end;
    }


    @Override
    public boolean next() {
        // emit buffered tokens

        if (emit > 0) {
            int token = count - emit - 1;
            int raw = raws[token];
            if (raw == INIT) return false; // found sentinel -> all tokens emitted

            boolean highEnoughDF = raw >= maxRaw - RELATIVE_RAW_THRESHOLD; // token has a high enough raw value to be emitted
            if (highEnoughDF) length = copyToken(token, buffer);

            emit--;
            if (emit == 0) {
                maxRaw     = raws[count - 1];
                raws[0]    = maxRaw;
                offsets[1] = copyToken(count - 1, tokens);
                count      = 1;
            }

            return highEnoughDF || next();
        }

        saveBeginEnd();

        // collect tokens until we reach a new position

        while (parent.next()) {
//            System.out.println("* " + parent); //XXX

            int len    = parent.length();
            int offset = offsets[count];

            raws[count] = minSketch.getRaw(parent.hash());

            count++;
            int size = offset + len;
            offsets[count] = size;

            if (len > buffer.length) buffer = new char[2 * len]; //XXX

            if (size > tokens.length) tokens = Arrays.copyOf(tokens, 2 * size); //XXX
            System.arraycopy(parent.buffer(), 0, tokens, offset, len);

//            System.out.println(parent + " --> "+ minSketch.getRaw(parent.hash()) + " " + begin() + "####");

//             System.out.println("BUFFER " + count + " " + new String(copies, 0, offsets[count])); // XXX

            if (parent.begin() >= lastEnd) { // we reached a new position
                if (lastEnd == INIT) saveBeginEnd();

                lastEnd = parent.end();
                emit    = count - 1;

//                 System.out.println("------------- " + emit); // XXX

                if (emit > 0) return next();
            }

            maxRaw = Math.max(maxRaw, raws[count - 1]);
        }

//        System.out.println("BUFFER " + count + " " + new String(tokens, 0, offsets[count])); // XXX

        // parent is exhausted, emit buffered tokens with sentinel
        raws[count] = INIT;
        emit = count + 1;
        count += 2;
        return next();
    }


    private int copyToken(int token, char[] target) {
        int offset = offsets[token];
        int len    = offsets[token + 1] - offset;
        System.arraycopy(tokens, offset, target, 0, len);
        return len;
    }


    private void saveBeginEnd() {
        begin = parent.begin();
        end   = parent.end  ();
    }

}
