package org.titiplex.align;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    @Test
    void tokenizeLineDropsLeadingNumericIdWhenRequested() {
        List<String> tokens = Tokenizer.tokenizeLine("12 ixim-na winh", true);
        assertEquals(List.of("ixim-na", "winh"), tokens);
    }

    @Test
    void tokenizeLineKeepsLeadingIdWhenNotRequested() {
        List<String> tokens = Tokenizer.tokenizeLine("12 ixim-na winh", false);
        assertEquals(List.of("12", "ixim-na", "winh"), tokens);
    }

    @Test
    void tokenizeWordSplitsOnHyphen() {
        assertEquals(List.of("ixim", "na"), Tokenizer.tokenizeWord("ixim-na"));
    }

    @Test
    void punctuationAndPairCostBehaveAsExpected() {
        assertTrue(Tokenizer.isPunctuation("."));
        assertFalse(Tokenizer.isPunctuation("ixim"));
        assertEquals(0, Tokenizer.pairCost(".", "."));
        assertTrue(Tokenizer.pairCost("ixim-na", "NOUN") > 0);
        assertTrue(Tokenizer.pairCost(".", "NOUN") >= 3);
    }
}
