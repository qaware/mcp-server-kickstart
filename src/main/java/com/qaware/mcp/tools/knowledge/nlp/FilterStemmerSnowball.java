package com.qaware.mcp.tools.knowledge.nlp;

import org.tartarus.snowball.SnowballProgram;

public class FilterStemmerSnowball extends Filter {

    private final SnowballProgram snowballProgram;


    public FilterStemmerSnowball(Tokens tokens, SnowballProgram aSnowballProgram) {
        super(tokens);

        snowballProgram = aSnowballProgram;
    }


    @Override
    protected void filter() {
        snowballProgram.setCurrent(buffer, length);

        snowballProgram.stem();
        assert snowballProgram.getCurrentBuffer() == buffer;

        length = snowballProgram.getCurrentBufferLength();
    }

}
