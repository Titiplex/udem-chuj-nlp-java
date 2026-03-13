package org.titiplex.model;

import java.util.List;

public record CorrectedBlock(
        int id,
        String chujText,
        String glossText,
        String translation,
        List<AlignedToken> alignedTokens
) {
    public CorrectedBlock {
        alignedTokens = List.copyOf(alignedTokens);
    }
}
