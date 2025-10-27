package com.qaware.mcp.tools.knowledge.nlp;

public class FilterEnglishPossessive extends Filter {

    public FilterEnglishPossessive(Tokens tokens) {
        super(tokens);
    }


    @Override
    protected void filter() {
        if (length >= 2) {
            char chr1 = buffer[length - 1];
            char chr2 = buffer[length - 2];
            if (    (chr2 == '\'' || chr2 == '\u2019' || chr2 == '\uFF07')
                 && (chr1 == 's' || chr1 == 'S')) {
                length -= 2;
            }
        }
    }

}
