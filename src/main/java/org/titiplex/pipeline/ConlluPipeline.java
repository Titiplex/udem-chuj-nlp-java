package org.titiplex.pipeline;

import org.titiplex.model.ConlluSentence;
import org.titiplex.model.ConlluToken;
import org.titiplex.model.CorrectedBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class ConlluPipeline {
    public ConlluSentence toConllu(CorrectedBlock block) {
        List<ConlluToken> tokens = new ArrayList<>();
        int id = 1;
        for (var aligned : block.alignedTokens()) {
            String form = aligned.chujSurface().isBlank() ? aligned.glossSurface() : aligned.chujSurface();
            String upos = guessUpos(form, aligned.glossSurface());
            String deprel = "PUNCT".equals(upos) ? "punct" : "_";
            tokens.add(new ConlluToken(
                    Integer.toString(id++),
                    form,
                    "_",
                    upos,
                    "_",
                    new LinkedHashMap<>(),
                    "0",
                    deprel,
                    "_",
                    aligned.glossSurface().isBlank() ? "_" : "Gloss=" + aligned.glossSurface()
            ));
        }

        return new ConlluSentence(Integer.toString(block.id()), block.chujText(), tokens);
    }

    private String guessUpos(String form, String gloss) {
        if (form == null || form.isBlank()) {
            return "_";
        }
        if (form.chars().allMatch(Character::isDigit)) {
            return "NUM";
        }
        if (form.length() == 1 && ".,;:!?()[]{}".contains(form)) {
            return "PUNCT";
        }
        if (gloss != null && gloss.matches("(?i).*(1sg|2sg|3sg|a1|a2|a3|b1|b2|b3|pron).*")) {
            return "PRON";
        }
        return "_";
    }
}
