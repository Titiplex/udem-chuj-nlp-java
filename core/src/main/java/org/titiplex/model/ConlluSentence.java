package org.titiplex.model;

import java.util.List;

public record ConlluSentence(
        String sentId,
        String text,
        List<ConlluToken> tokens
) {
    public ConlluSentence {
        tokens = List.copyOf(tokens);
    }
}
