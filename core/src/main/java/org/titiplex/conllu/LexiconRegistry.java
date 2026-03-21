package org.titiplex.conllu;

import java.util.*;

public final class LexiconRegistry {
    private final Map<String, Set<String>> lexicons = new LinkedHashMap<>();

    public void put(String name, Set<String> values) {
        Set<String> normed = new LinkedHashSet<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                normed.add(v.toLowerCase(Locale.ROOT));
            }
        }
        lexicons.put(name, Set.copyOf(normed));
    }

    public Set<String> get(String name) {
        return lexicons.getOrDefault(name, Set.of());
    }

    public boolean contains(String name, String value) {
        if (value == null) return false;
        return get(name).contains(value.toLowerCase(Locale.ROOT));
    }

    public Map<String, Set<String>> asMap() {
        return Map.copyOf(lexicons);
    }
}
