package com.qaware.mcp.tools.knowledge.nlp;

public class FilterDeduplicate extends Filter {

    private long lastHash;


    public FilterDeduplicate(Tokens tokens) {
        super(tokens);
    }


    @Override
    public Filter reset(CharSequence chars) {
        lastHash = 0;
        return super.reset(chars);
    }


    @Override
    public boolean next() {
        while (super.next()) {
            long hash = hash();
            if (hash == lastHash) continue;
            lastHash = hash;
            return true;
        }

        return false;
    }

}
