package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Locale;

public final class SplitSuffixRule implements CorrectionRule {
    private final String id;
    private final MatchSpec spec;
    private final List<String> suffixes;
    private final String glossPlacement; // left | right | duplicate

    public SplitSuffixRule(String id, MatchSpec spec, List<String> suffixes, String glossPlacement) {
        this.id = id;
        this.spec = spec;
        this.suffixes = suffixes == null ? List.of() : List.copyOf(suffixes);
        this.glossPlacement = glossPlacement == null ? "right" : glossPlacement.toLowerCase(Locale.ROOT);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        int i = 0;
        while (i < context.size()) {
            int target = TokenPatternMatcher.resolveTargetIndex(context.alignedTokens(), i, spec, context);
            if (target < 0) {
                i++;
                continue;
            }

            AlignedToken tok = context.get(target);
            String surface = tok.chujSurface();
            if (surface == null || surface.isBlank()) {
                i++;
                continue;
            }

            String matched = suffixes.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .filter(s -> surface.toLowerCase(Locale.ROOT).endsWith(s.toLowerCase(Locale.ROOT)))
                    .filter(s -> surface.length() > s.length())
                    .findFirst()
                    .orElse(null);

            if (matched == null) {
                i++;
                continue;
            }

            String leftSurface = surface.substring(0, surface.length() - matched.length());
            if (leftSurface.endsWith("-")) {
                leftSurface = leftSurface.substring(0, leftSurface.length() - 1);
            }

            AlignedToken left;
            AlignedToken right;

            switch (glossPlacement) {
                case "left" -> {
                    left = context.rebuildToken(RuleContext.splitSurface(leftSurface), tok.glossSegments());
                    right = context.rebuildToken(RuleContext.splitSurface(matched), List.of("_"));
                }
                case "duplicate" -> {
                    left = context.rebuildToken(RuleContext.splitSurface(leftSurface), tok.glossSegments());
                    right = context.rebuildToken(RuleContext.splitSurface(matched), tok.glossSegments());
                }
                default -> {
                    left = context.rebuildToken(RuleContext.splitSurface(leftSurface), List.of("_"));
                    right = context.rebuildToken(RuleContext.splitSurface(matched), tok.glossSegments());
                }
            }

            context.replace(target, left);
            context.alignedTokensMutable().add(target + 1, right);
            i = target + 2;
        }
    }
}
