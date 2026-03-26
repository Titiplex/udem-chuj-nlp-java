package org.titiplex.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.domain.validation.ValidationRun;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.persistence.repository.RuleRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RuleValidationService validationService;

    private RuleService service;

    @BeforeEach
    void setUp() {
        service = new RuleService(ruleRepository, validationService);
    }

    @Test
    void saveDefaultsKindAndPersistsValidatedRule() {
        Rule rule = rule(null, "r1", null, "rules:\n  - id: r1\n");
        when(validationService.validateRule(rule)).thenReturn(okRun());
        when(ruleRepository.save(rule)).thenReturn(rule);

        Rule saved = service.save(rule);

        assertThat(saved.getKind()).isEqualTo(RuleKind.CORRECTION);
        verify(validationService).validateRule(rule);
        verify(ruleRepository).save(rule);
    }

    @Test
    void saveRejectsInvalidRule() {
        Rule rule = rule(null, "broken", RuleKind.CORRECTION, "rules:\n  - id: broken\n");
        when(validationService.validateRule(rule)).thenReturn(errorRun("boom"));

        assertThatThrownBy(() -> service.save(rule))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        verify(ruleRepository, never()).save(any());
    }

    @Test
    void saveAllValidatesEveryRuleAndDefaultsMissingKinds() {
        Rule first = rule(null, "r1", null, "rules:\n  - id: r1\n");
        Rule second = rule(null, "r2", RuleKind.CONLLU, "rules:\n  - id: r2\n");

        when(validationService.validateRule(first)).thenReturn(okRun());
        when(validationService.validateRule(second)).thenReturn(okRun());

        service.saveAll(List.of(first, second));

        assertThat(first.getKind()).isEqualTo(RuleKind.CORRECTION);
        assertThat(second.getKind()).isEqualTo(RuleKind.CONLLU);
        verify(validationService).validateRule(first);
        verify(validationService).validateRule(second);
        verify(ruleRepository).saveAll(List.of(first, second));
    }

    @Test
    void saveAllRejectsBatchWhenOneRuleIsInvalid() {
        Rule first = rule(null, "r1", null, "rules:\n  - id: r1\n");
        Rule second = rule(null, "r2", null, "rules:\n  - id: r2\n");

        when(validationService.validateRule(first)).thenReturn(okRun());
        when(validationService.validateRule(second)).thenReturn(errorRun("invalid rule"));

        assertThatThrownBy(() -> service.saveAll(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("invalid rule");

        verify(ruleRepository, never()).saveAll(any());
    }

    @Test
    void importYamlUpdatesExistingRuleInsteadOfBlindInsert(@TempDir Path tempDir) throws Exception {
        Path yaml = tempDir.resolve("rules.yaml");
        Files.writeString(yaml, """
                rules:
                  - id: corr.normalize
                    name: Normalize
                    description: new body
                    rewrite:
                      after: heb'
                """);

        Rule existing = rule(10L, "corr.normalize", RuleKind.CORRECTION, "rules:\n  - id: old\n");
        existing.setName("Old name");
        existing.setVersionNo(3);
        existing.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));

        when(ruleRepository.findByStableId("corr.normalize")).thenReturn(Optional.of(existing));
        when(validationService.validateRule(any(Rule.class))).thenReturn(okRun());

        service.importYaml(yaml, RuleKind.CORRECTION);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Rule>> captor = ArgumentCaptor.forClass(List.class);
        verify(ruleRepository).saveAll(captor.capture());

        List<Rule> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        Rule merged = saved.get(0);
        assertThat(merged.getId()).isEqualTo(10L);
        assertThat(merged.getStableId()).isEqualTo("corr.normalize");
        assertThat(merged.getName()).isEqualTo("Normalize");
        assertThat(merged.getDescription()).isEqualTo("new body");
        assertThat(merged.getSourceFile()).isEqualTo("rules.yaml");
        assertThat(merged.getVersionNo()).isEqualTo(4);
    }

    @Test
    void importYamlRejectsInvalidImportedRule(@TempDir Path tempDir) throws Exception {
        Path yaml = tempDir.resolve("rules.yaml");
        Files.writeString(yaml, """
                rules:
                  - id: corr.normalize
                    name: Normalize
                    rewrite:
                      after: heb'
                """);

//        when(ruleRepository.findByStableId("corr.normalize")).thenReturn(Optional.empty());
        when(validationService.validateRule(any(Rule.class))).thenReturn(errorRun("invalid imported"));

        assertThatThrownBy(() -> service.importYaml(yaml, RuleKind.CORRECTION))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid imported rule 'corr.normalize': invalid imported");

        verify(ruleRepository, never()).saveAll(any());
    }

    private static Rule rule(Long id, String stableId, RuleKind kind, String yamlBody) {
        Rule rule = new Rule();
        rule.setId(id);
        rule.setStableId(stableId);
        rule.setName(stableId);
        rule.setKind(kind);
        rule.setEnabled(true);
        rule.setYamlBody(yamlBody);
        rule.setVersionNo(1);
        return rule;
    }

    private static ValidationRun okRun() {
        return new ValidationRun(null, Instant.now(), Instant.now(), true, "ok", List.of());
    }

    private static ValidationRun errorRun(String summary) {
        return new ValidationRun(null, Instant.now(), Instant.now(), false, summary, List.of());
    }
}
