package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;

public final class InsertSegmentRule implements CorrectionRule {
    private final String id;
    private final MatchSpec spec;
    private final String segment;
    private final int tokenIndex;
    private final int position;

    public InsertSegmentRule(String id, MatchSpec spec, String segment, int tokenIndex, int position) {
        this.id = id;
        this.spec = spec;
        this.segment = segment;
        this.tokenIndex = tokenIndex;
        this.position = position;
    }

    @Override public String id() { return id; }

    @Override
    public void apply(RuleContext context) {
        for (int i = 0; i < context.size(); i++) {
            int target = TokenPatternMatcher.resolveTargetIndex(context.alignedTokens(), i, spec, context);
            if (target < 0) continue;
            AlignedToken tok = context.get(target);
            List<String> segs = new ArrayList<>(tok.chujSegments().isEmpty() ? List.of(tok.chujSurface()) : tok.chujSegments());
            int idx = Math.max(0, Math.min(segs.size() - 1, tokenIndex - 1));
            String base = segs.get(idx);
            int pos = Math.max(0, Math.min(base.length(), position - 1));
            segs.set(idx, base.substring(0, pos) + segment + base.substring(pos));
            context.replace(target, context.rebuildToken(segs, tok.glossSegments()));
        }
    }
}
