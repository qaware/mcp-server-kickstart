package com.qaware.mcp.tools.knowledge.nlp;

public class FilterToLower extends Filter {

    public FilterToLower(Tokens tokens) {
        super(tokens);
    }


    @Override
    protected void filter() {
        length = Chars.toLower(buffer, 0, length);
    }

}
