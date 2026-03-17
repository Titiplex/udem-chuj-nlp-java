package org.titiplex.conllu;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectedBlock;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class AnnotationEngineTest {

    @Test
    void agreementHeuristicsProduceVerbAndAgreementFeats() {
        AnnotationConfig config = new AnnotationConfig();

        // Configure agreement extractor
        Pattern tagPattern = Pattern.compile(
            "^(?<series>(A|B))(?<person>(1|2|3))(?<number>PL)?$",
            Pattern.CASE_INSENSITIVE
        );

        List<AnnotationConfig.RoutingRule> routingRules = List.of(
            new AnnotationConfig.RoutingRule(
                "has(A) & has(B)",
                Map.of("SubCat", "Trans")
            ),
            new AnnotationConfig.RoutingRule(
                "has(B) & !has(A)",
                Map.of("SubCat", "Intrans")
            ),
            new AnnotationConfig.RoutingRule(
                "has(A) & !has(B)",
                Map.of("SubCat", "Trans")
            )
        );

        config.extractors().put(
            "agreement_verbs",
            new AnnotationConfig.ExtractorDef("agreement_verbs", tagPattern, routingRules)
        );

        // Add rule to identify verbs with agreement
        AnnotationRule verbRule = new AnnotationRule(
            "identify verbs from agreement",
            "token",
            100,
            null,
            java.util.Set.of(),
            false,
            "",
            "VERB",
            Map.of(),
            Map.of(),
            List.of(),
            List.of(
                Map.of(
                    "type", "scan_agreement",
                    "extractor", "agreement_verbs"
                )
            ),
            List.of(),
            List.of()
        );

        config.rules().add(verbRule);

        AnnotationEngine engine = new AnnotationEngine(config);

        CorrectedBlock block = new CorrectedBlock(
                1,
                "ix-naq",
                "A1-B2-go",
                "I see you",
                List.of(AlignedToken.of(
                        "ix-naq",
                        "A1-B2-go",
                        List.of("ix", "naq"),
                        List.of("A1", "B2", "go")
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
