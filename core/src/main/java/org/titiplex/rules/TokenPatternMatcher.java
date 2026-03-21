package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Locale;

public final class TokenPatternMatcher {
    private TokenPatternMatcher() {
    }

    public static boolean matchesAt(List<AlignedToken> tokens, int i, MatchSpec spec, RuleContext context) {
        return resolveTargetIndex(tokens, i, spec, context) >= 0;
    }

    public static int resolveTargetIndex(List<AlignedToken> tokens, int i, MatchSpec spec, RuleContext context) {
        if (i < 0 || i >= tokens.size()) return -1;
        if (!basicMatch(tokens, i, spec, context)) return -1;

        int target = resolveByTargets(i, spec.targets(), tokens.size());

        if (spec.rootLexiconRef() != null && !spec.rootLexiconRef().isBlank()) {
            int rootIdx = switch ((spec.anchorSide() == null ? "" : spec.anchorSide()).toLowerCase(Locale.ROOT)) {
                case "j" -> Math.min(i + 1, tokens.size() - 1);
                case "i" -> i;
                default -> i;
            };
            if (rootIdx < 0 || rootIdx >= tokens.size()) return -1;
            if (!context.hasLexiconMatchAt(rootIdx, spec.rootLexiconRef())) return -1;
        }

        if (spec.rootStartsWithVowel()) {
            int rootIdx = switch ((spec.anchorSide() == null ? "" : spec.anchorSide()).toLowerCase(Locale.ROOT)) {
                case "j" -> Math.min(i + 1, tokens.size() - 1);
                default -> i;
            };
            if (!context.startsWithVowelAt(rootIdx)) return -1;
        }

        return target;
    }


    private static boolean basicMatch(List<AlignedToken> tokens, int i, MatchSpec spec, RuleContext context) {
        AlignedToken t = tokens.get(i);
        if (!spec.glossValues().isEmpty() && spec.glossValues().stream().noneMatch(g -> containsIgnoreCase(t.glossSegments(), g) || eq(t.glossSurface(), g)))
            return false;
        if (!spec.glossStartsWith().isEmpty() && spec.glossStartsWith().stream().noneMatch(g -> t.glossSegments().stream().anyMatch(s -> norm(s).startsWith(norm(g)))))
            return false;
        if (spec.lexiconRef() != null && !spec.lexiconRef().isBlank() && !context.tokenMatchesLexicon(t, spec.lexiconRef()))
            return false;
        if (!spec.tokenIsWord().isEmpty() && spec.tokenIsWord().stream().noneMatch(w -> eq(t.chujSurface(), w)))
            return false;
        if (!spec.tokenAny().isEmpty() && spec.tokenAny().stream().noneMatch(w -> containsIgnoreCase(t.chujSegments(), w) || eq(t.chujSurface(), w)))
            return false;
        if (!spec.tokenStartsWith().isEmpty() && spec.tokenStartsWith().stream().noneMatch(w -> norm(t.chujSurface()).startsWith(norm(w))))
            return false;
        if (!spec.tokenEndsWith().isEmpty() && spec.tokenEndsWith().stream().noneMatch(w -> norm(t.chujSurface()).endsWith(norm(w))))
            return false;
        if (!spec.tokenHasSegment().isEmpty() && spec.tokenHasSegment().stream().noneMatch(w -> containsIgnoreCase(t.chujSegments(), w)))
            return false;
        if (spec.tokenStartsWithVowel() && !startsWithVowel(t.chujSurface())) return false;
        if (spec.betweenLength() != null && spec.betweenLength() != 1) return false;
        if (!spec.tokenSequences().isEmpty() && spec.tokenSequences().stream().noneMatch(seq -> sequenceMatches(tokens, i, seq)))
            return false;
        return true;
    }

    private static int resolveByTargets(int i, String targets, int size) {
        if (targets == null || targets.isBlank()) return i;
        return switch (targets.toLowerCase(Locale.ROOT)) {
            case "i" -> i;
            case "j" -> Math.min(i + 1, size - 1);
            default -> i;
        };
    }

    public static boolean sequenceMatches(List<AlignedToken> tokens, int i, List<String> seq) {
        if (seq.isEmpty()) return true;
        if (i + seq.size() > tokens.size()) return false;
        for (int k = 0; k < seq.size(); k++) {
            String expected = seq.get(k);
            if ("_".equals(expected)) continue;
            if (!eq(tokens.get(i + k).chujSurface(), expected)) return false;
        }
        return true;
    }

    private static boolean startsWithVowel(String s) {
        if (s == null || s.isBlank()) return false;
        char c = Character.toLowerCase(s.charAt(0));
        return "aeiou".indexOf(c) >= 0;
    }

    private static boolean containsIgnoreCase(List<String> values, String needle) {
        return values.stream().anyMatch(v -> eq(v, needle));
    }

    private static boolean eq(String a, String b) {
        return norm(a).equals(norm(b));
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
