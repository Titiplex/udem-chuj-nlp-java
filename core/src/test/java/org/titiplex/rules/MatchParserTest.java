package org.titiplex.rules;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchParserTest {

    @Test
    void parseSupportsGlossAnyStartsWithAndLexiconTogether() {
        Map<String, Object> rawRule = Map.of("targets", "i");
        Map<String, Object> rewrite = Map.of(
                "match", Map.of(
                        "gloss", Map.of(
                                "any", List.of("FUT", "PROSP"),
                                "starts_with", List.of("DIR"),
                                "in_lexicon", "directionals"
                        )
                )
        );

        MatchSpec spec = MatchParser.parse(rawRule, rewrite);

        assertEquals(List.of("FUT", "PROSP"), spec.glossValues());
        assertEquals(List.of("DIR"), spec.glossStartsWith());
        assertEquals("directionals", spec.lexiconRef());
    }
}