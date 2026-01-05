package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.tools.knowledge.nlp.Filter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinguisticTest {

    @Test
    public void test() {
        verify("!!!  #Running# informationEN's AutomatioN   THE ihr das la grünste-ruNNing going hellen\tinFOrmation\rhellste\nder letzte existS daß mueßte ÄÖßßßßßßÜ",
            """
            run 292069 6 13 Running

            information 57143 15 30 informationEN's

            autom 14120 31 41 AutomatioN

            grun 38006 59 66 grünste

            run 292069 67 74 ruNNing

            go 231348 75 80 going

            hell 22500 81 87 hellen
            hellen 4159 81 87 hellen

            information 57143 88 99 inFOrmation
            inform 194246 88 99 inFOrmation

            hell 22500 100 107 hellste

            letzt 292069 112 118 letzte

            exist 102330 119 125 existS

            musst 259941 130 136 mueßte

            aossssssssssssu 0 137 146 ÄÖßßßßßßÜ
            """);

        verify("explosion",
            """
            explosion 9955 0 9 explosion
            explos 21227 0 9 explosion
            """);

        verify("", "");
    }


    private static void verify(String text, String expected) {
        Filter filter = Linguistic.newFilter().reset(text);

        StringBuilder stringBuilder = new StringBuilder();
        for (int lastEnd = -1; filter.next();) {

            int begin = filter.begin();
            int end   = filter.end();

            if (begin >= lastEnd) {
                lastEnd = end;
                if (! stringBuilder.isEmpty()) stringBuilder.append('\n');
            }

            stringBuilder.append(filter + " " + Linguistic.getDF(filter.hash())  + " " + begin + " " + end + " " + filter.source().subSequence(begin, end) + "\n");
        }

        assertEquals(expected, stringBuilder.toString());

        assertFalse(filter.next());
    }

}
