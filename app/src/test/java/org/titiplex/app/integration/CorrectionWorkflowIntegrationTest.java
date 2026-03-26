package org.titiplex.app.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.app.persistence.repository.RawEntryRepository;
import org.titiplex.app.persistence.repository.RuleRepository;
import org.titiplex.app.service.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        RawEntryService.class,
        CorrectedEntryService.class,
        DesktopPipelineFactory.class,
        AutoCorrectionService.class,
        RulesetFingerprintService.class,
        CorrectedEntryStalenessService.class
})
class CorrectionWorkflowIntegrationTest {

    @Autowired
    private RawEntryRepository rawEntryRepository;

    @Autowired
    private CorrectedEntryRepository correctedEntryRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RawEntryService rawEntryService;

    @Autowired
    private CorrectedEntryService correctedEntryService;

    @Autowired
    private AutoCorrectionService autoCorrectionService;

    @Autowired
    private CorrectedEntryStalenessService stalenessService;

    @Test
    void approvedEntryCanBecomeStaleForRulesAndRawThenBeRegeneratedAsDraft() {
        RawEntry raw = new RawEntry();
        raw.setRawText("ha tin");
        raw.setGlossText("A1 win");
        raw.setTranslationText("I win");
        raw = rawEntryService.save(raw);

        CorrectedEntry draft = autoCorrectionService.applyToRawEntry(raw);
        draft.setIsCorrect(true);
        CorrectedEntry approved = correctedEntryService.save(draft);

        assertFalse(approved.isStale());
        assertNotNull(approved.getApprovedRulesFingerprint());
        assertNotNull(approved.getApprovedRawUpdatedAt());

        Rule rule = new Rule();
        rule.setStableId("normalize_demo");
        rule.setName("Normalize demo");
        rule.setKind(RuleKind.CORRECTION);
        rule.setEnabled(true);
        rule.setVersionNo(1);
        rule.setYamlBody("""
                rules:
                  - id: normalize_demo
                    name: Normalize demo
                    rewrite:
                      regex_sub:
                        scope: chuj
                        pattern: "^x$"
                        repl: "x"
                """);
        ruleRepository.save(rule);

        int changedByRules = stalenessService.markApprovedEntriesStaleIfRulesChanged();
        assertEquals(1, changedByRules);

        CorrectedEntry afterRuleChange = correctedEntryRepository.findById(approved.getId()).orElseThrow();
        assertTrue(afterRuleChange.isStale());
        assertTrue(afterRuleChange.isStaleDueToRules());
        assertEquals("Rules changed", afterRuleChange.stalenessReasonLabel());

        raw.setRawText("ha toj");
        raw = rawEntryService.save(raw);

        CorrectedEntry afterRawChange = correctedEntryRepository.findById(approved.getId()).orElseThrow();
        assertTrue(afterRawChange.isStale());
        assertTrue(afterRawChange.isStaleDueToRules());
        assertTrue(afterRawChange.isStaleDueToRaw());
        assertEquals("Raw + rules changed", afterRawChange.stalenessReasonLabel());

        CorrectedEntry regenerated = autoCorrectionService.regenerateDraftFromRaw(afterRawChange.getId());

        assertNotEquals(Boolean.TRUE, regenerated.getIsCorrect());
        assertFalse(regenerated.isStale());
        assertFalse(regenerated.isStaleDueToRaw());
        assertFalse(regenerated.isStaleDueToRules());
        assertNull(regenerated.getApprovedRawUpdatedAt());
        assertNull(regenerated.getApprovedRulesFingerprint());
        assertEquals("", regenerated.stalenessReasonLabel());
        assertEquals("ha toj", regenerated.getRawText());
    }
}
