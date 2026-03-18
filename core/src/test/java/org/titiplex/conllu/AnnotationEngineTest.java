package org.titiplex.conllu;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectedBlock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class AnnotationEngineTest {

    @Test
    void agreementRulesFromConfigProduceVerbAndAgreementFeats() throws IOException {
        Path yaml = Path.of("src/test/resources/annotation/test-annotation.yaml");
        AnnotationConfig config = (new AnnotationConfigLoader()).load(yaml);

        AnnotationEngine engine = new AnnotationEngine(config);

        CorrectedBlock block = new CorrectedBlock(
                1,
                "ix-naq",
                "A1-B2-ganar",
                "I beat you",
                List.of(AlignedToken.of(
                        "ix-naq",
                        "A1-B2-ganar",
                        List.of("ix", "naq"),
                        List.of("A1", "B2", "ganar")
                ))
        );

        ConlluEntry entry = engine.annotate(block);
        assertEquals(1, entry.lines().size());

        ConlluLine line = entry.lines().get(0);
        assertEquals("VERB", line.upos());
        assertEquals("1", line.feats().get("Pers[subj]"));
        assertEquals("2", line.feats().get("Pers[obj]"));
        assertEquals("Trans", line.feats().get("SubCat"));
    }

    @Test
    void glossMappingSetsUposAndFeatures() {
        AnnotationConfig config = new AnnotationConfig();
        config.glossMapper().putPos("noun", "NOUN");
        config.glossMapper().putFeat("pl", java.util.Map.of("Number", "Plur"));
        AnnotationEngine engine = new AnnotationEngine(config);

        CorrectedBlock block = new CorrectedBlock(
                2,
                "winh-ob",
                "noun-pl",
                "men",
                List.of(AlignedToken.of(
                        "winh-ob",
                        "noun-pl",
                        List.of("winh", "ob"),
                        List.of("noun", "pl")
                ))
        );

        ConlluEntry entry = engine.annotate(block);
        ConlluLine line = entry.lines().get(0);

        assertEquals("NOUN", line.upos());
        assertEquals("Plur", line.feats().get("Number"));
        assertEquals("winh-ob", line.form());
    }
}
