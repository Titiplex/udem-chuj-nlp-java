package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

public final class SimpleLemmatizer {
    public String lemmaFor(AlignedToken token, String upos) {
        if ("PUNCT".equals(upos)) return token.chujSurface();
        for (String seg : token.chujSegments()) {
            if (seg == null || seg.isBlank()) continue;
            String cleaned = seg.replaceAll("^[=<>-]+|[=<>-]+$", "");
            if (!cleaned.isBlank()) return cleaned;
        }
        if (!token.chujSurface().isBlank()) return token.chujSurface();
        if (!token.glossSegments().isEmpty()) return token.glossSegments().get(0);
        return "_";
    }
}
