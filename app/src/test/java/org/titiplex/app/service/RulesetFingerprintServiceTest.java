package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.persistence.repository.RuleRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RulesetFingerprintServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RulesetFingerprintService service;

    @Test
    void returnsConstantForEmptyCorrectionRuleset() {
        when(ruleRepository.findAllByEnabledTrueAndKindOrderByStableIdAsc(RuleKind.CORRECTION)).thenReturn(List.of());

        assertEquals(RulesetFingerprintService.EMPTY_CORRECTION_RULESET, service.currentCorrectionRulesetFingerprint());
    }

    @Test
    void changesWhenRuleYamlChanges() {
        when(ruleRepository.findAllByEnabledTrueAndKindOrderByStableIdAsc(RuleKind.CORRECTION))
                .thenReturn(List.of(rule("r1", 1, "rules:\n  - id: r1\n    name: R1\n")));

        String first = service.currentCorrectionRulesetFingerprint();

        when(ruleRepository.findAllByEnabledTrueAndKindOrderByStableIdAsc(RuleKind.CORRECTION))
                .thenReturn(List.of(rule("r1", 1, "rules:\n  - id: r1\n    name: R1 changed\n")));

        String second = service.currentCorrectionRulesetFingerprint();

        assertNotEquals(first, second);
    }

    private static Rule rule(String stableId, int versionNo, String yamlBody) {
        Rule rule = new Rule();
        rule.setStableId(stableId);
        rule.setVersionNo(versionNo);
        rule.setKind(RuleKind.CORRECTION);
        rule.setEnabled(true);
        rule.setYamlBody(yamlBody);
        return rule;
    }
}
