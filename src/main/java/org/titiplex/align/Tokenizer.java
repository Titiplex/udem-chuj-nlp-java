package org.titiplex.align;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Tokenizer {
    private static final Set<Character> PUNCT = Set.of(
            ',', '.', ';', ':', '!', '?', '(', ')', '[', ']', '{', '}',
            '"', '\'', '«', '»', '“', '”', '–', '—'
    );

    private Tokenizer() {
    }

    public static List<String> tokenizeLine(String text, boolean dropLeadingId) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> tokens = Arrays.stream(text.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));

        if (dropLeadingId && !tokens.isEmpty() && tokens.getFirst().matches("\\d+\\.?")) {
            tokens.removeFirst();
        }

        return tokens;
    }

    public static List<String> tokenizeWord(String word) {
        if (word == null || word.isBlank()) {
            return List.of();
        }
        return Arrays.stream(word.split("-"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    public static boolean isPunctuation(String token) {
        return token != null
                && !token.isBlank()
                && token.chars().allMatch(c -> PUNCT.contains((char) c));
    }

    public static int morphCount(String word) {
        if (word == null || word.isBlank()) {
            return 0;
        }
        return (int) Arrays.stream(word.split("-")).filter(s -> !s.isBlank()).count();
    }

    public static int pairCost(String chuj, String gloss) {
        String left = chuj == null ? "" : chuj;
        String right = gloss == null ? "" : gloss;

        if (left.isEmpty() && right.isEmpty()) {
            return 0;
        }
        if (isPunctuation(left) && isPunctuation(right)) {
            return 0;
        }
        if (isPunctuation(left) != isPunctuation(right)) {
            return 3;
        }

        int cost = Math.abs(morphCount(left) - morphCount(right));
        if (left.isEmpty() != right.isEmpty()) {
            cost += 2;
        }
        return cost;
    }
}
