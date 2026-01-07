package com.qaware.mcp.tools.knowledge;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpusTest {

    @Test
    void filesystemScannerPopulatesCorpus() throws URISyntaxException {
        // given
        Path resourceDir = Paths.get(getClass().getClassLoader().getResource("corpus-test").toURI());
        Corpus corpus = new Corpus(new FileSystemScanner(resourceDir.toString()));

        // then
        verify( """
                🟡 FILE*/Text.txt
                Das ist ein anderes Dokument im Test.
                Hier kommt noch ganz viel mehr Text hinzu, um es länger
                ➖➖
                
                🟡 FILE*/sub/Word.docx
                # Hello from corpus test file one.
                This text contains the stuff that should be found by the Corpus test.
                 | Some more text. | Cell
                 | Row 2a | Row 2b
                ➖➖
                """, corpus.getAll());

        // and
        verify( """
                🟡 FILE*/Text.txt
                anderes Dokument im Test.
                Hier kommt noch ganz viel mehr Text hinzu, um es länger
                ➖➖
                
                🟡 FILE*/sub/Word.docx
                text contains the stuff
                ➖➖                
                """, corpus.getPassages("der ist the a -#Dökumenten!. _CONTaININg's", 15));
    }


    private static void verify(String expected, String actual) {
        assertEquals(normalize(expected), normalize(actual));
    }


    private static String normalize(String normalized) {
        return normalized.replaceAll("/SOURCE.*?corpus-test", "*").replace("\r", "").trim();
    }

}

