package org.titiplex.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MatchParser {
    private MatchParser() {
    }

    public static MatchSpec parse(Map<String, Object> rawRule, Map<String, Object> rewrite) {
        Map<String, Object> match = RuleYamlSupport.map(rewrite.get("match"));
        Map<String, Object> between = RuleYamlSupport.map(rewrite.get("between"));
        Map<String, Object> tokenMap = RuleYamlSupport.map(match.get("tokens"));
        Map<String, Object> glossMap = RuleYamlSupport.map(match.get("gloss"));
        Map<String, Object> surfaceMap = RuleYamlSupport.map(match.get("surface"));
        Object tokenDirect = match.get("tokens");
        List<List<String>> sequences = new ArrayList<>();

        if (tokenDirect instanceof List<?> l && !l.isEmpty() && l.get(0) instanceof List<?>) {
            for (Object o : l) {
                sequences.add(RuleYamlSupport.stringList(o));
            }
        } else if (tokenDirect instanceof List<?> && !RuleYamlSupport.stringList(tokenDirect).isEmpty() && tokenMap.isEmpty()) {
            sequences.add(RuleYamlSupport.stringList(tokenDirect));
        }
        if (tokenMap.containsKey("isword")) {
            for (String s : RuleYamlSupport.stringList(tokenMap.get("isword"))) {
                sequences.add(List.of(s));
            }
        }

        List<String> glossValues = new ArrayList<>();
        Object glossObj = match.get("gloss");
        if (glossObj instanceof String g) {
            glossValues.add(g);
        } else if (glossObj instanceof Map<?, ?>) {
            Object any = glossMap.containsKey("any")
                    ? glossMap.get("any")
                    : glossMap.get("values");
            glossValues.addAll(RuleYamlSupport.stringList(any));
        } else {
            glossValues.addAll(RuleYamlSupport.stringList(glossObj));
        }

        String lexiconRef = RuleYamlSupport.string(glossMap.get("in_lexicon"), null);
        String rootLexiconRef = RuleYamlSupport.string(surfaceMap.get("root_in_lexicon"), null);

        return new MatchSpec(
                sequences,
                RuleYamlSupport.stringList(tokenMap.get("isword")),
                RuleYamlSupport.stringList(tokenMap.get("any")),
                RuleYamlSupport.stringList(tokenMap.get("startswith")),
                RuleYamlSupport.stringList(tokenMap.get("endswith")),
                RuleYamlSupport.stringList(tokenMap.get("has_segment")),
                RuleYamlSupport.bool(tokenMap.get("startswith_vowel"), false),
                glossValues,
                RuleYamlSupport.stringList(glossMap.get("starts_with")),
                lexiconRef,
                between.containsKey("length") ? Integer.parseInt(between.get("length").toString()) : null,
                RuleYamlSupport.string(rawRule.get("targets"), null),
                RuleYamlSupport.string(surfaceMap.get("side"), null),
                rootLexiconRef,
                RuleYamlSupport.bool(surfaceMap.get("root_startswith_vowel"), false)
        );
    }
}
