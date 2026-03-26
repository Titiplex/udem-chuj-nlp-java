package org.titiplex.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.app.persistence.repository.RawEntryRepository;
import org.titiplex.rules.RuleEngine;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoCorrectionServiceTest {

    @Mock
    private DesktopPipelineFactory pipelineFactory;

    @Mock
    private CorrectedEntryRepository correctedEntryRepository;

    @Mock
    private RawEntryRepository rawEntryRepository;

    @Mock
    private RulesetFingerprintService rulesetFingerprintService;

    private AutoCorrectionService service;

    @BeforeEach
    void setUp() {
        service = new AutoCorrectionService(
                pipelineFactory,
                correctedEntryRepository,
                rawEntryRepository,
                rulesetFingerprintService
        );
    }

    @Test
    void returnsExistingApprovedEntryWithoutReprocessingWhenStillCurrent() {
        RawEntry raw = rawEntry(1L, "ha tin", "A1 win", "I win");
        raw.setUpdatedAt(Instant.parse("2026-03-26T10:00:00Z"));
        CorrectedEntry approved = correctedEntry(raw, true);
        approved.setApprovedRawUpdatedAt(Instant.parse("2026-03-26T10:00:00Z"));
        approved.setApprovedRulesFingerprint("fp");

        when(correctedEntryRepository.findByRawEntryId(1L)).thenReturn(Optional.of(approved));
        when(rulesetFingerprintService.isCorrectionRulesetOutdated(approved)).thenReturn(false);

        CorrectedEntry result = service.applyToRawEntry(raw);

        assertThat(result).isSameAs(approved);
        verifyNoInteractions(pipelineFactory);
        verify(correctedEntryRepository, never()).save(any());
    }

    @Test
    void marksApprovedEntryStaleWhenRulesChange() {
        RawEntry raw = rawEntry(1L, "ha tin", "A1 win", "I win");
        raw.setUpdatedAt(Instant.parse("2026-03-26T10:00:00Z"));

        CorrectedEntry approved = correctedEntry(raw, true);
        approved.setApprovedRawUpdatedAt(Instant.parse("2026-03-26T10:00:00Z"));
        approved.setApprovedRulesFingerprint("old");

        when(correctedEntryRepository.findByRawEntryId(1L)).thenReturn(Optional.of(approved));
        when(rulesetFingerprintService.isCorrectionRulesetOutdated(approved)).thenReturn(true);
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorrectedEntry result = service.applyToRawEntry(raw);

        assertThat(result).isSameAs(approved);
        assertThat(result.isStale()).isTrue();
        assertThat(result.isStaleDueToRules()).isTrue();
        verifyNoInteractions(pipelineFactory);
    }

    @Test
    void createsNewCorrectedEntryWhenNoneExists() {
        RawEntry raw = rawEntry(7L, "ha tin", "A1 win", "I win");
        when(correctedEntryRepository.findByRawEntryId(7L)).thenReturn(Optional.empty());
        when(pipelineFactory.createRuleEngine()).thenReturn(new RuleEngine(List.of()));
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorrectedEntry saved = service.applyToRawEntry(raw);

        assertThat(saved.getRawEntry()).isSameAs(raw);
        assertThat(saved.getRawText()).isEqualTo("ha tin");
        assertThat(saved.getGlossText()).isEqualTo("A1 win");
        assertThat(saved.getTranslationText()).isEqualTo("I win");
        assertThat(saved.getIsCorrect()).isFalse();
        verify(correctedEntryRepository).save(any(CorrectedEntry.class));
    }

    @Test
    void updatesExistingNonApprovedEntry() {
        RawEntry raw = rawEntry(9L, "ha tin", "A1 win", "I win");
        CorrectedEntry pending = correctedEntry(raw, false);
        pending.setRawText("old");
        pending.setGlossText("old");
        pending.setTranslationText("old");

        when(correctedEntryRepository.findByRawEntryId(9L)).thenReturn(Optional.of(pending));
        when(pipelineFactory.createRuleEngine()).thenReturn(new RuleEngine(List.of()));
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorrectedEntry saved = service.applyToRawEntry(raw);

        assertThat(saved).isSameAs(pending);
        assertThat(saved.getRawText()).isEqualTo("ha tin");
        assertThat(saved.getGlossText()).isEqualTo("A1 win");
        assertThat(saved.getTranslationText()).isEqualTo("I win");
        assertThat(saved.getDescription()).contains("Generated from raw entry #9");
    }

    @Test
    void regenerateDraftFromRawClearsApprovalAndStaleReasonFlags() {
        RawEntry raw = rawEntry(5L, "ha tin", "A1 win", "I win");
        CorrectedEntry approved = correctedEntry(raw, true);
        approved.setId(77L);
        approved.setStale(true);
        approved.setStaleDueToRaw(true);
        approved.setStaleDueToRules(true);
        approved.setApprovedRawUpdatedAt(Instant.parse("2026-03-26T10:00:00Z"));
        approved.setApprovedRulesFingerprint("old-fp");

        when(correctedEntryRepository.findById(77L)).thenReturn(Optional.of(approved));
        when(rawEntryRepository.findById(5L)).thenReturn(Optional.of(raw));
        when(pipelineFactory.createRuleEngine()).thenReturn(new RuleEngine(List.of()));
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorrectedEntry saved = service.regenerateDraftFromRaw(77L);

        assertThat(saved.getIsCorrect()).isFalse();
        assertThat(saved.isStale()).isFalse();
        assertThat(saved.isStaleDueToRaw()).isFalse();
        assertThat(saved.isStaleDueToRules()).isFalse();
        assertThat(saved.getApprovedRawUpdatedAt()).isNull();
        assertThat(saved.getApprovedRulesFingerprint()).isNull();
        assertThat(saved.getDescription()).contains("Regenerated draft");
    }

    @Test
    void applyToAllReturnsProcessedCount() {
        RawEntry first = rawEntry(1L, "a", "b", "c");
        RawEntry second = rawEntry(2L, "d", "e", "f");

        when(correctedEntryRepository.findByRawEntryId(anyLong())).thenReturn(Optional.empty());
        when(pipelineFactory.createRuleEngine()).thenReturn(new RuleEngine(List.of()));
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int count = service.applyToAll(List.of(first, second));

        assertThat(count).isEqualTo(2);
        verify(correctedEntryRepository, times(2)).save(any(CorrectedEntry.class));
    }

    private static RawEntry rawEntry(Long id, String rawText, String glossText, String translationText) {
        RawEntry raw = new RawEntry();
        raw.setId(id);
        raw.setRawText(rawText);
        raw.setGlossText(glossText);
        raw.setTranslationText(translationText);
        return raw;
    }

    private static CorrectedEntry correctedEntry(RawEntry raw, Boolean approved) {
        CorrectedEntry corrected = new CorrectedEntry();
        corrected.setRawEntry(raw);
        corrected.setIsCorrect(approved);
        return corrected;
    }
}