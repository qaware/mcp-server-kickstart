package com.qaware.mcp.tools.knowledge.nlp;

import java.util.function.Predicate;

public class FilterConditional extends Filter {

    private final Predicate<Filter> predicate;


    public FilterConditional(Tokens tokens, Predicate<Filter> aPredicate) {
        super(tokens);

        predicate = aPredicate;
    }


    @Override
    public boolean next() {
        while (super.next()) if (predicate.test(this)) return true;

        return false;
    }

}
