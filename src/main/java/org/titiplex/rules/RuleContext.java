package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;

public final class RuleContext {
    private final List<AlignedToken> alignedTokens;

    public RuleContext(List<AlignedToken> alignedTokens) {
        this.alignedTokens = new ArrayList<>(alignedTokens);
    }

    public List<AlignedToken> alignedTokens() {
        return List.copyOf(alignedTokens);
    }

    public void replace(int index, AlignedToken token) {
        alignedTokens.set(index, token);
    }

    public void removeAt(int index) {
        alignedTokens.remove(index);
    }
}
