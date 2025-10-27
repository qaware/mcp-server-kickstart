package com.qaware.mcp.tools.knowledge.nlp;

import java.util.ArrayList;
import java.util.List;

public class FilterCombine extends Filter {

    private static final int INIT = -1;

    private int pos;

    private boolean inner;
    private boolean allow;

    private List<Tokens> delegates = new ArrayList<>();

    private int copyLength;
    private char[] copyBuffer = {};


    public FilterCombine(Tokens tokens) {
        super(tokens);
    }


    @Override
    public Filter reset(CharSequence chars) {
        if (chars == this) {
            return this;
        }

        setPos(INIT);

        delegates.forEach(tokens -> reset(this));

        return super.reset(chars);
    }


    @Override
    public boolean next() {
        if (pos == INIT) {
            if (! super.next()) {
                return false;
            }

            setPos(0);
        }

        if (inner) {
            if (allow) {
                allow = false;
                return true;
            }

            return false;
        }

        Tokens delegate = delegates.get(pos);

        inner = true;
        boolean next = delegate.next();
        inner = false;

        if (next) {
            accept(delegate);
            return true;
        }

        setPos(pos + 1);

        if (pos == delegates.size()) {
            pos = INIT;
        } else {
            length = copyLength;
            copy(copyBuffer, buffer);
        }

        return next();
    }


    public void combine(Filter filter) {
        delegates.add(filter);
    }


    @Override
    protected void filter() {
        if (length > copyBuffer.length) copyBuffer = new char[length * 3 / 2];

        copyLength = length;
        copy(buffer, copyBuffer);
    }


    private void setPos(int newPos) {
        pos = newPos;
        allow = true;
    }

}
