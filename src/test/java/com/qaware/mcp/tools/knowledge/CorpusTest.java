package com.qaware.mcp.tools.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class CorpusTest {

    @Test
    void filesystemScannerPopulatesCorpus() throws URISyntaxException {
        // given
        Path resourceDir = Paths.get(getClass().getClassLoader().getResource("corpus-test").toURI());
        Corpus corpus = new Corpus(new FileSystemScanner(resourceDir.toString()));

        // check getAll
        verify( """
                🟡 FILE*/Text.txt
                Ein Dokument auf Deutsch.
                Es kommt noch etwas Fülltext dazu, um es ein bisschen länger
                ➖➖

                🟡 FILE*/sub/Word.docx
                # Hello from corpus test file one.

                This text contains the stuff that should be found by the *Corpus* test.
                 | Some more **text**. | Cell
                 | Row 2a | Row 2b
                ➖➖
                """,
                corpus.getAll());

        // verify that mixed language queries work
        verify( """
                🟡 FILE*/Text.txt
                Dokument auf Deutsch.
                Es kommt noch etwas Fülltext dazu, um es ein bisschen länger
                ➖➖

                🟡 FILE*/sub/Word.docx
                text contains the stuff
                ➖➖
                """,
                corpus.getPassages("contained der ist the a -#Dökumenten!. _CONTaININg's", 12));

        // verify that using terms multiple times gives them a higher impact
        verify( """
                🟡 FILE*/sub/Word.docx
                text contains the stuff
                ➖➖
                """,
                corpus.getPassages("contained fufu contains deutsch contains ", 3));

        // verify that queries with only stopwords give no results
        verify("", corpus.getPassages("der die das and or the", 100));
    }


    private static void verify(String expected, String actual) {
        assertEquals(normalize(expected), normalize(actual));
    }


    private static String normalize(String normalized) {
        return normalized.replaceAll("/SOURCE.*?corpus-test", "*").replace("\r", "").trim();
    }

}

