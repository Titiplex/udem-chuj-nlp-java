package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectedBlock;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AnnotationEngine {
    private final AnnotationConfig config;
    private final SimpleLemmatizer lemmatizer = new SimpleLemmatizer();

    public AnnotationEngine(AnnotationConfig config) {
        this.config = config;
    }

    public ConlluEntry annotate(CorrectedBlock block) {
        ConlluEntry entry = new ConlluEntry();
        entry.headers().addAll(ConlluEntry.makeHeaders(
                Integer.toString(block.id()),
                block.chujText(),
                block.translation(),
                block.glossText()
        ));

        int id = 1;
        for (AlignedToken token : block.alignedTokens()) {
            if (token.chujSurface().isBlank() && token.glossSurface().isBlank()) continue;
            ConlluLine line = ConlluLine.basic(Integer.toString(id++), token.chujSurface().isBlank() ? token.glossSurface() : token.chujSurface());
            line.setMisc(token.glossSurface().isBlank() ? "_" : "Gloss=" + token.glossSurface());

            Map<String, Object> ctx = baseContext(token, line);

            for (AnnotationRule rule : config.rules()) {
                if (rule.matches(token, config, ctx)) {
                    rule.apply(line, token, config, ctx);
                }
            }

            applyGlossMap(token, line);

            if ("_".equals(line.upos())) line.setUpos(guessUpos(token));
            line.setLemma(lemmatizer.lemmaFor(token, line.upos()));
            if ("PUNCT".equals(line.upos())) line.setDeprel("punct");
            entry.lines().add(line);
        }
        return entry;
    }

    private Map<String, Object> baseContext(AlignedToken token, ConlluLine line) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("form", line.form());
        ctx.put("gloss", token.glossSurface());
        ctx.put("surface", token.chujSurface());
        return ctx;
    }

    private void applyGlossMap(AlignedToken token, ConlluLine line) {
        for (String gloss : token.glossSegments()) {
            String pos = config.glossMapper().resolvePos(gloss);
            if (pos != null && "_".equals(line.upos())) line.setUpos(pos);
            line.putAllFeats(config.glossMapper().resolveFeats(gloss), false);
        }
    }

    private String guessUpos(AlignedToken token) {
        String form = token.chujSurface().isBlank() ? token.glossSurface() : token.chujSurface();
        if (form == null || form.isBlank()) return "_";
        if (form.chars().allMatch(Character::isDigit)) return "NUM";
        if (form.length() == 1 && ".,;:!?()[]{}¡¿—\"'«»".contains(form)) return "PUNCT";
        for (String gloss : token.glossSegments()) {
            String pos = config.glossMapper().resolvePos(gloss);
            if (pos != null) return pos;
        }
        return "_";
    }
}
