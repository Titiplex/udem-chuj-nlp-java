package org.titiplex.rules;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SplitSuffixRuleTest {

    @Test
    void splitSuffixWithRightPlacement() {
        AlignedToken token = AlignedToken.of("stem-ta", "GLOSS", List.of("stem", "ta"), List.of("GLOSS"));
        RuleContext context = new RuleContext(List.of(token));

        MatchSpec spec = new MatchSpec(
                List.of(List.of("stem-ta")),
                List.of("stem-ta"),
                List.of(), List.of(), List.of(), List.of(), false,
                List.of(), List.of(),
                null, null, null, null, null, false
        );

        new SplitSuffixRule("x", spec, List.of("ta"), "right").apply(context);

        assertEquals(2, context.alignedTokens().size());
        assertEquals("stem", context.alignedTokens().get(0).chujSurface());
        assertEquals("_", context.alignedTokens().get(0).glossSurface());
        assertEquals("ta", context.alignedTokens().get(1).chujSurface());
        assertEquals("GLOSS", context.alignedTokens().get(1).glossSurface());
    }

    @Test
    void splitSuffixWithLeftPlacement() {
        AlignedToken token = AlignedToken.of("root-kot", "DIR", List.of("root", "kot"), List.of("DIR"));
        RuleContext context = new RuleContext(List.of(token));

        MatchSpec spec = new MatchSpec(
                List.of(List.of("root-kot")),
                List.of("root-kot"),
                List.of(), List.of(), List.of(), List.of(), false,
                List.of(), List.of(),
                null, null, null, null, null, false
        );

        new SplitSuffixRule("x", spec, List.of("kot"), "left").apply(context);

        assertEquals(2, context.alignedTokens().size());
        assertEquals("root", context.alignedTokens().get(0).chujSurface());
        assertEquals("DIR", context.alignedTokens().get(0).glossSurface());
        assertEquals("kot", context.alignedTokens().get(1).chujSurface());
        assertEquals("_", context.alignedTokens().get(1).glossSurface());
    }

    @Test
    void splitSuffixWithDuplicatePlacement() {
        AlignedToken token = AlignedToken.of("base-kan", "MOVE", List.of("base", "kan"), List.of("MOVE"));
        RuleContext context = new RuleContext(List.of(token));

        MatchSpec spec = new MatchSpec(
                List.of(List.of("base-kan")),
                List.of("base-kan"),
                List.of(), List.of(), List.of(), List.of(), false,
                List.of(), List.of(),
                null, null, null, null, null, false
        );

        new SplitSuffixRule("x", spec, List.of("kan"), "duplicate").apply(context);

        assertEquals(2, context.alignedTokens().size());
        assertEquals("base", context.alignedTokens().get(0).chujSurface());
        assertEquals("MOVE", context.alignedTokens().get(0).glossSurface());
        assertEquals("kan", context.alignedTokens().get(1).chujSurface());
        assertEquals("MOVE", context.alignedTokens().get(1).glossSurface());
    }
}
