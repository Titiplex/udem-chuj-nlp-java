package org.titiplex.app.infra.yaml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class YamlEITest {

    private final YamlEI yamlEI = new YamlEI();

    @Test
    void readCorrectionRulesBuildsOneEntityPerRule(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("rules.yaml");
        Files.writeString(input, """
                rules:
                  - id: corr.one
                    name: Corr One
                    rewrite:
                      after: heb'
                  - id: corr.two
                    name: Corr Two
                    rewrite:
                      after: hin
                """);

        List<Rule> rules = yamlEI.readRules(input, RuleKind.CORRECTION);

        assertThat(rules).hasSize(2);
        assertThat(rules.get(0).getStableId()).isEqualTo("corr.one");
        assertThat(rules.get(0).getYamlBody()).contains("corr.one");
        assertThat(rules.get(0).getYamlBody()).doesNotContain("def:");
        assertThat(rules.get(1).getStableId()).isEqualTo("corr.two");
    }

    @Test
    void readConlluRulesCopiesSharedSectionsIntoEachRule(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("annotation.yaml");
        Files.writeString(input, """
                def:
                  pos: [ "VERB" ]
                extractors:
                  agr:
                    type: regex
                rules:
                  - id: conllu.one
                    when:
                      gloss: [ "A1" ]
                    set:
                      upos: VERB
                  - id: conllu.two
                    when:
                      gloss: [ "B2" ]
                    set:
                      upos: PRON
                """);

        List<Rule> rules = yamlEI.readRules(input, RuleKind.CONLLU);

        assertThat(rules).hasSize(2);
        assertThat(rules.get(0).getYamlBody()).contains("def:");
        assertThat(rules.get(0).getYamlBody()).contains("extractors:");
        assertThat(rules.get(0).getYamlBody()).contains("conllu.one");
        assertThat(rules.get(1).getYamlBody()).contains("conllu.two");
    }

    @Test
    void writeConlluRulesMergesSharedSectionsAndFlattensRules(@TempDir Path tempDir) throws Exception {
        Rule first = rule("one", RuleKind.CONLLU, """
                def:
                  pos: [ "VERB" ]
                extractors:
                  agr:
                    type: regex
                rules:
                  - id: conllu.one
                    set:
                      upos: VERB
                """);
        Rule second = rule("two", RuleKind.CONLLU, """
                def:
                  feats: [ "Person" ]
                rules:
                  - id: conllu.two
                    set:
                      upos: PRON
                """);

        Path output = tempDir.resolve("out.yaml");
        yamlEI.writeRules(List.of(first, second), output, RuleKind.CONLLU);

        Map<String, Object> map = new Yaml().load(Files.readString(output));
        assertThat(map).containsKeys("def", "rules");
        assertThat(((Map<String, ?>) map.get("def")).keySet()).contains("pos", "feats");
        assertThat((List<?>) map.get("rules")).hasSize(2);
    }

    @Test
    void writeCorrectionRulesExportsFlatRulesOnly(@TempDir Path tempDir) throws Exception {
        Rule first = rule("one", RuleKind.CORRECTION, """
                rules:
                  - id: corr.one
                    rewrite:
                      after: heb'
                """);
        Rule second = rule("two", RuleKind.CORRECTION, """
                rules:
                  - id: corr.two
                    rewrite:
                      after: hin
                """);

        Path output = tempDir.resolve("out.yaml");
        yamlEI.writeRules(List.of(first, second), output, RuleKind.CORRECTION);

        Map<String, Object> map = new Yaml().load(Files.readString(output));
        assertThat(map).containsOnlyKeys("rules");
        assertThat((List<?>) map.get("rules")).hasSize(2);
    }

    private static Rule rule(String stableId, RuleKind kind, String yamlBody) {
        Rule rule = new Rule();
        rule.setStableId(stableId);
        rule.setName(stableId);
        rule.setKind(kind);
        rule.setEnabled(true);
        rule.setYamlBody(yamlBody);
        rule.setVersionNo(1);
        return rule;
    }
}
