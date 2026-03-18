package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;

public final class MergeSequenceRule implements CorrectionRule {
    private final String id;
    private final List<List<String>> sequences;

    public MergeSequenceRule(String id, List<List<String>> sequences) {
        this.id = id;
        this.sequences = sequences;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        int i = 0;
        while (i < context.size() - 1) {
            List<AlignedToken> snap = context.alignedTokens();
            List<String> matched = null;
            for (List<String> seq : sequences) {
                if (seq.size() == 2 && TokenPatternMatcher.sequenceMatches(snap, i, seq)) { matched = seq; break; }
            }
            if (matched == null) { i++; continue; }
            AlignedToken a = context.get(i);
            AlignedToken b = context.get(i + 1);
            List<String> ch = new ArrayList<>(a.chujSegments()); ch.addAll(b.chujSegments());
            List<String> gl = new ArrayList<>(a.glossSegments()); gl.addAll(b.glossSegments());
            context.replace(i, context.rebuildToken(ch, gl));
            context.removeAt(i + 1);
        }
    }
}
