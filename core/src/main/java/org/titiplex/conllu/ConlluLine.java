package org.titiplex.conllu;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class ConlluLine {
    private String id = "_";
    private String form = "_";
    private String lemma = "_";
    private String upos = "_";
    private String xpos = "_";
    private final Map<String, String> feats = new LinkedHashMap<>();
    private String head = "0";
    private String deprel = "_";
    private String deps = "_";
    private String misc = "_";

    public static ConlluLine basic(String id, String form) {
        ConlluLine line = new ConlluLine();
        line.id = norm(id);
        line.form = norm(form);
        return line;
    }

    public void putFeat(String key, String value, boolean overwrite) {
        if (key == null || key.isBlank() || value == null || value.isBlank()) return;
        if (overwrite || !feats.containsKey(key)) feats.put(key, value);
    }

    public void putAllFeats(Map<String, String> values, boolean overwrite) {
        if (values == null) return;
        for (var e : values.entrySet()) putFeat(e.getKey(), e.getValue(), overwrite);
    }

    public String toConlluLine() {
        String featsString = feats.isEmpty() ? "_" : new TreeMap<>(feats).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("|"));
        return String.join("\t", norm(id), norm(form), norm(lemma), norm(upos), norm(xpos), featsString, norm(head), norm(deprel), norm(deps), norm(misc));
    }

    private static String norm(String s) { return s == null || s.isBlank() ? "_" : s; }

    public String id() { return id; }
    public String form() { return form; }
    public String lemma() { return lemma; }
    public String upos() { return upos; }
    public String xpos() { return xpos; }
    public Map<String, String> feats() { return feats; }
    public String head() { return head; }
    public String deprel() { return deprel; }
    public String deps() { return deps; }
    public String misc() { return misc; }
    public void setLemma(String lemma) { this.lemma = norm(lemma); }
    public void setUpos(String upos) { this.upos = norm(upos); }
    public void setDeprel(String deprel) { this.deprel = norm(deprel); }
    public void setMisc(String misc) { this.misc = norm(misc); }
}
