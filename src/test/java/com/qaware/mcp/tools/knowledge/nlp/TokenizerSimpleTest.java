package com.qaware.mcp.tools.knowledge.nlp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TokenizerSimpleTest {

    @Test
    void test() {
        verify("hello wor=ld dash-splits  .  0", "#hello#wor=ld#dash#splits#0#");
        verify(" .hel.lo. ", "#hel.lo#");
    }


    private static void verify(String input, String expected) {
        StringBuilder stringBuilder = new StringBuilder("#");

        for (Tokens tokenizer = new TokenizerSimple().reset(input); tokenizer.next();) {
            String string = tokenizer.toString();

            for (int i = 0; i < tokenizer.length(); i++) {
                assertEquals(tokenizer.buffer()[i], string.charAt(i));
            }

            stringBuilder.append(string).append('#');
        }

        assertEquals(expected, stringBuilder.toString());
    }

}
