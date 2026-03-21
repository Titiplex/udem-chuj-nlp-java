package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.*;

public final class GlossBeforeAfterRule implements CorrectionRule {
    private final String id;
    private final MatchSpec spec;
    private final Map<String, String> mapping;
    private final boolean ignoreCase;

    public GlossBeforeAfterRule(String id,
                                MatchSpec spec,
                                Map<String, String> mapping,
                                boolean ignoreCase) {
        this.id = id;
        this.spec = spec;
        this.mapping = Map.copyOf(mapping);
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        for (int i = 0; i < context.size(); i++) {
            int target = TokenPatternMatcher.resolveTargetIndex(context.alignedTokens(), i, spec, context);
            if (target < 0) {
                continue;
            }

            AlignedToken token = context.get(target);

            String surfaceKey = normalize(token.glossSurface());
            String surfaceReplacement = mapping.get(surfaceKey);

            if (surfaceReplacement != null) {
                List<String> newGloss = splitSurface(surfaceReplacement);
                context.replace(target, context.rebuildToken(token.chujSegments(), newGloss));
                continue;
            }

            List<String> newGloss = new ArrayList<>();
            boolean changed = false;

            for (String gloss : token.glossSegments()) {
                String key = normalize(gloss);
                String replacement = mapping.get(key);
                if (replacement != null) {
                    newGloss.add(replacement);
                    changed = true;
                } else {
                    newGloss.add(gloss);
                }
            }

            if (changed) {
                context.replace(target, context.rebuildToken(token.chujSegments(), newGloss));
            }
        }
    }

    public static Map<String, String> buildMapping(List<String> before,
                                                   List<String> after,
                                                   boolean ignoreCase) {
        Map<String, String> out = new HashMap<>();
        if (before == null || after == null || before.isEmpty() || after.isEmpty()) {
            return out;
        }

        if (after.size() == 1) {
            String target = after.getFirst();
            for (String b : before) {
                out.put(ignoreCase ? normalizeStatic(b) : b, target);
            }
            return out;
        }

        for (int i = 0; i < Math.min(before.size(), after.size()); i++) {
            String key = ignoreCase ? normalizeStatic(before.get(i)) : before.get(i);
            out.put(key, after.get(i));
        }

        return out;
    }

    private List<String> splitSurface(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("-"));
    }

    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        return ignoreCase ? normalizeStatic(s) : s;
    }

    private static String normalizeStatic(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}