package org.titiplex.conllu;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class GlossMapper {
    private final Map<String, String> posMap = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> featMap = new LinkedHashMap<>();

    public void putPos(String gloss, String upos) {
        if (gloss != null && upos != null) posMap.put(gloss.toLowerCase(Locale.ROOT), upos);
    }

    public void putFeat(String gloss, Map<String, String> feats) {
        if (gloss != null && feats != null && !feats.isEmpty())
            featMap.put(gloss.toLowerCase(Locale.ROOT), Map.copyOf(feats));
    }

    public String resolvePos(String gloss) {
        return gloss == null ? null : posMap.get(gloss.toLowerCase(Locale.ROOT));
    }

    public Map<String, String> resolveFeats(String gloss) {
        return gloss == null ? Map.of() : featMap.getOrDefault(gloss.toLowerCase(Locale.ROOT), Map.of());
    }
}
