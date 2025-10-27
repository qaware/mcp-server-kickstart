package com.qaware.mcp.tools.knowledge.nlp;

import com.qaware.mcp.tools.knowledge.quantization.CountMinSketch;

// hier muss ich nochmal drueber nachdenken, das funktioniert prima, ist aber echt fies!
public class FilterDisambiguate extends Filter {

    private int lastEnd = -1;

    private char[] copies = new char[2048];

    private int count;

    private int[] offsets = new int[10];

    private int[] raws = new int[10];
    private int maxRaw = 0;

    private int index = 0;

    private final CountMinSketch minSketch;

    private int begin;
    private int end;


    public FilterDisambiguate(Tokens tokens, CountMinSketch aMinSketch) {
        super(tokens);

        minSketch = aMinSketch;

        buffer = new char[2048];
    }


    @Override
    public Filter reset(CharSequence chars) {
        index   =  0;
        lastEnd = -1;
        count   =  0;
        maxRaw  =  0;
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
        if (index > 0) {
            index--;

            int idx = count - index - 2;

            boolean skip = raws[idx] < maxRaw - 50;
//            skip = false;
//            System.out.println(raws[idx] +  " " + maxRaw + " " + skip);

            length = 1;
            if (! skip) length = copy(idx, buffer);
            if (length == 0) return false;

            if (index == 0) {
                maxRaw = raws[count - 1];
                raws[0] = maxRaw;
                offsets[1] = copy(count - 1, copies);
                count = 1;
            }

            return !skip || next();
        }

        begin = parent.begin();
        end   = parent.end  ();

        while (parent.next()) {
//            System.out.println("* " + parent); //XXX

            int len    = parent.length();
            int offset = offsets[count];

            raws[count] = minSketch.getRaw(parent.hash());

            count++;
            offsets[count] = offset + len;
            System.arraycopy(parent.buffer(), 0, copies, offset, len); // XXX resize copies

//            System.out.println(parent + " --> "+ minSketch.getRaw(parent.hash()) + " " + begin() + "####");

//             System.out.println("BUFFER " + count + " " + new String(copies, 0, offsets[count])); // XXX

            if (parent.begin() >= lastEnd) {
                if (lastEnd == -1) {
                    begin = parent.begin();
                    end   = parent.end  ();
                }

                lastEnd = parent.end();
                index   = count - 1;

                // System.out.println("------------- " + emit); // XXX

                if (index > 0) return next();
            }

            maxRaw = Math.max(maxRaw, raws[count - 1]);
        }

//        System.out.println("BUFFER " + count + " " + new String(copies, 0, offsets[count])); // XXX

        index = count;
        offsets[index + 1] = offsets[index];
        count++;
        return next();
    }


    private int copy(int idx, char[] target) {
        int offset = offsets[idx    ];
        int len    = offsets[idx + 1] - offset;
        System.arraycopy(copies, offset, target, 0, len);
        return len;
    }

}
