package org.titiplex.conllu;

import java.util.ArrayList;
import java.util.List;

public final class ConlluEntry {
    private final List<String> headers = new ArrayList<>();
    private final List<ConlluLine> lines = new ArrayList<>();

    public List<String> headers() {
        return headers;
    }

    public List<ConlluLine> lines() {
        return lines;
    }

    public String toConlluString() {
        List<String> out = new ArrayList<>(headers);
        for (ConlluLine line : lines) out.add(line.toConlluLine());
        out.add("");
        return String.join("\n", out);
    }

    public static List<String> makeHeaders(String sentId, String text, String translation, String gloss) {
        List<String> headers = new ArrayList<>();
        if (sentId != null && !sentId.isBlank()) headers.add("# sent_id = " + sentId);
        if (text != null && !text.isBlank()) headers.add("# text = " + text);
        if (gloss != null && !gloss.isBlank()) headers.add("# gloss = " + gloss);
        if (translation != null && !translation.isBlank()) headers.add("# translation = " + translation);
        return headers;
    }
}
