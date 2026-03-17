package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.*;

public final class RuleContext {
    private final List<AlignedToken> alignedTokens;
    private final Map<String, Set<String>> lexicons;

    public RuleContext(List<AlignedToken> alignedTokens) {
        this(alignedTokens, Map.of());
    }

    public RuleContext(List<AlignedToken> alignedTokens, Map<String, Set<String>> lexicons) {
        this.alignedTokens = new ArrayList<>(alignedTokens);
        this.lexicons = lexicons == null ? Map.of() : lexicons;
    }

    public List<AlignedToken> alignedTokens() {
        return List.copyOf(alignedTokens);
    }

    List<AlignedToken> alignedTokensMutable() {
        return alignedTokens;
    }

    public int size() {
        return alignedTokens.size();
    }

    public AlignedToken get(int index) {
        return alignedTokens.get(index);
    }

    public void replace(int index, AlignedToken token) {
        alignedTokens.set(index, token);
    }

    public void removeAt(int index) {
        alignedTokens.remove(index);
    }

    public AlignedToken rebuildToken(List<String> surfaceSegments, List<String> glossSegments) {
        return AlignedToken.of(
                String.join("-", surfaceSegments),
                String.join("-", glossSegments),
                surfaceSegments,
                glossSegments
        );
    }

    public static List<String> splitSurface(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("-"));
    }

    public boolean startsWithVowelAt(int index) {
        if (index < 0 || index >= alignedTokens.size()) return false;
        String s = alignedTokens.get(index).chujSurface();
        if (s == null || s.isBlank()) return false;
        char c = Character.toLowerCase(s.charAt(0));
        return "aeiou".indexOf(c) >= 0;
    }

    public boolean tokenMatchesLexicon(AlignedToken token, String lexiconName) {
        Set<String> lexicon = lexicons.getOrDefault(lexiconName, Set.of());
        for (String gloss : token.glossSegments()) {
            if (lexicon.contains(norm(gloss))) return true;
        }
        for (String seg : token.chujSegments()) {
            if (lexicon.contains(norm(seg))) return true;
        }
        return lexicon.contains(norm(token.chujSurface())) || lexicon.contains(norm(token.glossSurface()));
    }

    public boolean hasLexiconMatchAt(int index, String lexiconName) {
        if (index < 0 || index >= alignedTokens.size()) return false;
        return tokenMatchesLexicon(alignedTokens.get(index), lexiconName);
    }

    private static String norm(String v) {
        return v == null ? "" : v.toLowerCase(Locale.ROOT);
    }
}