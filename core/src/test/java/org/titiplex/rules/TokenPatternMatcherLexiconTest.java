package org.titiplex.rules;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TokenPatternMatcherLexiconTest {

    @Test
    void matchesGlossAgainstConfiguredLexicon() {
        AlignedToken token = AlignedToken.of(
                "form",
                "ganar",
                List.of("form"),
                List.of("ganar")
        );

        RuleContext context = new RuleContext(
                List.of(token),
                Map.of("spanish_verbs", Set.of("ganar", "pasar"))
        );

        MatchSpec spec = new MatchSpec(
                List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), false,
                List.of(), List.of(),
                "spanish_verbs",
                null,
                null,
                null,
                null,
                false
        );

        assertEquals(0, TokenPatternMatcher.resolveTargetIndex(List.of(token), 0, spec, context));
    }
}
