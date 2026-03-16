package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Locale;

public final class SplitDirectionalRule implements CorrectionRule {
    private final String id;
    private final MatchSpec spec;
    private final List<String> splitTokens;

    public SplitDirectionalRule(String id, MatchSpec spec, List<String> splitTokens) {
        this.id = id;
        this.spec = spec;
        this.splitTokens = splitTokens;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        int i = 0;
        while (i < context.size()) {
            int target = TokenPatternMatcher.resolveTargetIndex(context.alignedTokens(), i, spec);
            if (target < 0) {
                i++;
                continue;
            }
            AlignedToken tok = context.get(target);
            if (tok.glossSegments().stream().noneMatch(g -> g.equalsIgnoreCase("DIR") || g.toLowerCase(Locale.ROOT).startsWith("dir."))) {
                i++;
                continue;
            }
            String surface = tok.chujSurface();
            String matched = splitTokens.stream()
                    .filter(sf -> !sf.isBlank()
                            && surface.toLowerCase(Locale.ROOT).endsWith(sf.toLowerCase(Locale.ROOT))
                            && surface.length() > sf.length())
                    .findFirst()
                    .orElse(null);
            if (matched == null) {
                i++;
                continue;
            }
            String root = surface.substring(0, surface.length() - matched.length());
            AlignedToken left = context.rebuildToken(List.of(root), List.of("_"));
            AlignedToken right = context.rebuildToken(List.of(matched), tok.glossSegments());
            context.replace(target, left);
            context.alignedTokensMutable().add(target + 1, right);
            i = target + 2;
        }
    }
}
