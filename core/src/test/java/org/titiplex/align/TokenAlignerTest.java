package org.titiplex.align;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class TokenAlignerTest {

    private final TokenAligner aligner = new TokenAligner();

    @Test
    void alignsTokensInOrderForSimpleSentence() {
        List<AlignedToken> aligned = aligner.align(
                List.of("ixim-na", "winh"),
                List.of("NOUN-DET", "man")
        );

        assertEquals(2, aligned.size());
        assertEquals("ixim-na", aligned.get(0).chujSurface());
        assertEquals("NOUN-DET", aligned.get(0).glossSurface());
        assertEquals(List.of("ixim", "na"), aligned.get(0).chujSegments());
        assertEquals(List.of("NOUN", "DET"), aligned.get(0).glossSegments());
    }

    @Test
    void preservesUnmatchedGlossAsGlossOnlyToken() {
        List<AlignedToken> aligned = aligner.align(
                List.of("ixim"),
                List.of("NOUN", "DET")
        );

        assertEquals(2, aligned.size());
        assertEquals("ixim", aligned.get(0).chujSurface());
        assertEquals("NOUN", aligned.get(0).glossSurface());
        assertEquals("", aligned.get(1).chujSurface());
        assertEquals("DET", aligned.get(1).glossSurface());
    }

    @Test
    void tieBreakStrategyCanBeConfigured() {
        TokenAligner aligner = new TokenAligner(2, 2, AlignmentTieBreakStrategy.MATCH_FIRST);

        List<AlignedToken> aligned = aligner.align(
                List.of("ixim"),
                List.of("NOUN", "DET")
        );

        assertEquals(2, aligned.size());
    }

    @Test
    void glossGapFirstPrefersKeepingTrailingGlossUnmatched() {
        TokenAligner aligner = new TokenAligner(2, 2, AlignmentTieBreakStrategy.GLOSS_GAP_FIRST);

        List<AlignedToken> aligned = aligner.align(
                List.of("ixim"),
                List.of("NOUN", "DET")
        );

        assertEquals(2, aligned.size());
        assertEquals("ixim", aligned.get(0).chujSurface());
        assertEquals("NOUN", aligned.get(0).glossSurface());
        assertEquals("", aligned.get(1).chujSurface());
        assertEquals("DET", aligned.get(1).glossSurface());
    }
}
