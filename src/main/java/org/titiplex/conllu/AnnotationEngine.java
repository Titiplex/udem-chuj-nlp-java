package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectedBlock;

import java.util.Locale;

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
            if (token.chujSurface().isBlank() && token.glossSurface().isBlank()) {
                continue;
            }

            ConlluLine line = ConlluLine.basic(
                    Integer.toString(id++),
                    token.chujSurface().isBlank() ? token.glossSurface() : token.chujSurface()
            );
            line.setMisc(token.glossSurface().isBlank() ? "_" : "Gloss=" + token.glossSurface());

            applyGlossMap(token, line);
            applyAgreementHeuristics(token, line);

            for (AnnotationRule rule : config.rules()) {
                if (rule.matches(token)) {
                    rule.apply(line, token, config);
                }
            }

            if ("_".equals(line.upos())) {
                line.setUpos(guessUpos(token));
            }
            line.setLemma(lemmatizer.lemmaFor(token, line.upos()));
            if ("PUNCT".equals(line.upos())) {
                line.setDeprel("punct");
            }
            entry.lines().add(line);
        }
        return entry;
    }

    private void applyGlossMap(AlignedToken token, ConlluLine line) {
        for (String gloss : token.glossSegments()) {
            String pos = config.glossMapper().resolvePos(gloss);
            if (pos != null && "_".equals(line.upos())) {
                line.setUpos(pos);
            }
            line.putAllFeats(config.glossMapper().resolveFeats(gloss), false);
        }
    }

    private void applyAgreementHeuristics(AlignedToken token, ConlluLine line) {
        String aPerson = null, bPerson = null;
        String aNum = "Sing", bNum = "Sing";
        for (String gloss : token.glossSegments()) {
            String g = gloss.toUpperCase(Locale.ROOT);
            if (g.matches("A[123](SG|PL)?")) {
                aPerson = g.substring(1, 2);
                if (g.endsWith("PL")) {
                    aNum = "Plur";
                }
            }
            if (g.matches("B[123](SG|PL)?")) {
                bPerson = g.substring(1, 2);
                if (g.endsWith("PL")) {
                    bNum = "Plur";
                }
            }
        }
        if (aPerson == null && bPerson == null) {
            return;
        }
        line.setUpos("VERB");
        if (aPerson != null && bPerson != null) {
            line.putFeat("Pers[subj]", aPerson, false);
            line.putFeat("Number[subj]", aNum, false);
            line.putFeat("Pers[obj]", bPerson, false);
            line.putFeat("Number[obj]", bNum, false);
            line.putFeat("SubCat", "Trans", false);
        } else if (bPerson != null) {
            line.putFeat("Pers[subj]", bPerson, false);
            line.putFeat("Number[subj]", bNum, false);
            line.putFeat("SubCat", "Intrans", false);
        } else {
            line.putFeat("Pers[subj]", aPerson, false);
            line.putFeat("Number[subj]", aNum, false);
            line.putFeat("Pers[obj]", "3", false);
            line.putFeat("Number[obj]", "Sing", false);
            line.putFeat("SubCat", "Trans", false);
        }
    }

    private String guessUpos(AlignedToken token) {
        String form = token.chujSurface().isBlank() ? token.glossSurface() : token.chujSurface();
        if (form == null || form.isBlank()) {
            return "_";
        }
        if (form.chars().allMatch(Character::isDigit)) {
            return "NUM";
        }
        if (form.length() == 1 && ".,;:!?()[]{}¡¿—\"".contains(form)) {
            return "PUNCT";
        }
        for (String gloss : token.glossSegments()) {
            String lower = gloss.toLowerCase(Locale.ROOT);
            if (lower.matches("a[123].*|b[123].*")) {
                return "VERB";
            }
            String pos = config.glossMapper().resolvePos(gloss);
            if (pos != null) {
                return pos;
            }
            if (lower.matches(".*(ar|er|ir)$")) {
                return "VERB";
            }
        }
        return "_";
    }
}
