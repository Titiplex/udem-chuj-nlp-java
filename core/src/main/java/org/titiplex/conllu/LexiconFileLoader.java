package org.titiplex.conllu;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class LexiconFileLoader {
    private LexiconFileLoader() {
    }

    public static Set<String> load(Path path) throws IOException {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".txt") || name.endsWith(".lst")) {
            return loadText(path);
        }
        if (name.endsWith(".csv")) {
            return loadCsv(path);
        }
        if (name.endsWith(".json")) {
            return loadJson(path);
        }
        return loadText(path);
    }

    private static Set<String> loadText(Path path) throws IOException {
        Set<String> out = new LinkedHashSet<>();
        for (String line : Files.readAllLines(path)) {
            String s = line.trim();
            if (!s.isBlank()) out.add(s);
        }
        return out;
    }

    private static Set<String> loadCsv(Path path) throws IOException {
        Set<String> out = new LinkedHashSet<>();
        List<String> lines = Files.readAllLines(path);
        if (lines.isEmpty()) return out;

        String[] header = splitCsv(lines.get(0));
        int chosenColumn = 0;
        for (int i = 0; i < header.length; i++) {
            String h = header[i].trim().toLowerCase();
            if (h.equals("lemma") || h.equals("word") || h.equals("form") || h.equals("surface") || h.equals("entry")) {
                chosenColumn = i;
                break;
            }
        }

        for (int i = 1; i < lines.size(); i++) {
            String[] row = splitCsv(lines.get(i));
            if (row.length <= chosenColumn) continue;
            String value = row[chosenColumn].trim();
            if (!value.isBlank()) out.add(value);
        }
        return out;
    }

    private static Set<String> loadJson(Path path) throws IOException {
        Object data = new Yaml().load(Files.readString(path));
        Set<String> out = new LinkedHashSet<>();
        flattenJsonLike(data, out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void flattenJsonLike(Object data, Set<String> out) {
        if (data == null) return;
        if (data instanceof String s) {
            if (!s.isBlank()) out.add(s);
            return;
        }
        if (data instanceof List<?> list) {
            for (Object item : list) flattenJsonLike(item, out);
            return;
        }
        if (data instanceof Map<?, ?> map) {
            if (map.containsKey("entries") && map.get("entries") instanceof List<?> l) {
                for (Object item : l) flattenJsonLike(item, out);
                return;
            }
            if (map.containsKey("words") && map.get("words") instanceof List<?> l) {
                for (Object item : l) flattenJsonLike(item, out);
                return;
            }
            for (Object value : map.values()) flattenJsonLike(value, out);
        }
    }

    private static String[] splitCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
