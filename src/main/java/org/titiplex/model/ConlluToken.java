package org.titiplex.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record ConlluToken(
        String id,
        String form,
        String lemma,
        String upos,
        String xpos,
        Map<String, String> feats,
        String head,
        String deprel,
        String deps,
        String misc
) {
    public ConlluToken {
        feats = feats == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(feats));
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "_" : value;
    }

    private String featsAsString() {
        if (feats.isEmpty()) {
            return "_";
        }
        return feats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("|"));
    }

    public String toConlluLine() {
        return String.join("\t",
                normalize(id),
                normalize(form),
                normalize(lemma),
                normalize(upos),
                normalize(xpos),
                featsAsString(),
                normalize(head),
                normalize(deprel),
                normalize(deps),
                normalize(misc)
        );
    }
}
