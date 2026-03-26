package org.titiplex.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.rules.RuleEngine;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoCorrectionServiceTest {

    @Mock
    private DesktopPipelineFactory pipelineFactory;

    @Mock
    private CorrectedEntryRepository correctedEntryRepository;

    private AutoCorrectionService service;

    @BeforeEach
    void setUp() {
        service = new AutoCorrectionService(pipelineFactory, correctedEntryRepository);
    }

    @Test
    void returnsExistingApprovedEntryWithoutReprocessingWhenRawDidNotMove() {
        Instant snapshot = Instant.parse("2026-03-25T12:00:00Z");
        RawEntry raw = rawEntry(1L, "ha tin", "A1 win", "I win", snapshot);
        CorrectedEntry approved = correctedEntry(raw, true);
        approved.setApprovedRawUpdatedAt(snapshot);
        approved.setStale(false);

        when(correctedEntryRepository.findByRawEntryId(1L)).thenReturn(Optional.of(approved));

        CorrectedEntry result = service.applyToRawEntry(raw);

        assertThat(result).isSameAs(approved);
        assertThat(result.isStale()).isFalse();
        verifyNoInteractions(pipelineFactory);
        verify(correctedEntryRepository, never()).save(any());
    }

    @Test
    void marksApprovedEntryStaleWhenRawMovedAfterApproval() {
        Instant approvedAt = Instant.parse("2026-03-25T12:00:00Z");
        Instant rawUpdatedAt = Instant.parse("2026-03-26T12:00:00Z");
        RawEntry raw = rawEntry(1L, "ha tin", "A1 win", "I win", rawUpdatedAt);
        CorrectedEntry approved = correctedEntry(raw, true);
        approved.setApprovedRawUpdatedAt(approvedAt);
        approved.setStale(false);

        when(correctedEntryRepository.findByRawEntryId(1L)).thenReturn(Optional.of(approved));
        when(correctedEntryRepository.save(approved)).thenReturn(approved);

        CorrectedEntry result = service.applyToRawEntry(raw);

        assertThat(result).isSameAs(approved);
        assertThat(result.isStale()).isTrue();
        verifyNoInteractions(pipelineFactory);
        verify(correctedEntryRepository).save(approved);
    }

    @Test
    void createsNewCorrectedEntryWhenNoneExists() {
        RawEntry raw = rawEntry(7L, "ha tin", "A1 win", "I win", Instant.parse("2026-03-25T12:00:00Z"));
        when(correctedEntryRepository.findByRawEntryId(7L)).thenReturn(Optional.empty());
        when(pipelineFactory.createRuleEngine()).thenReturn(new RuleEngine(List.of()));
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorrectedEntry saved = service.applyToRawEntry(raw);

        assertThat(saved.getRawEntry()).isSameAs(raw);
        assertThat(saved.getRawText()).isEqualTo("ha tin");
        assertThat(saved.getGlossText()).isEqualTo("A1 win");
        assertThat(saved.getTranslationText()).isEqualTo("I win");
        assertThat(saved.getIsCorrect()).isFalse();
        assertThat(saved.isStale()).isFalse();
        assertThat(saved.getApprovedRawUpdatedAt()).isNull();
        verify(correctedEntryRepository).save(any(CorrectedEntry.class));
    }

    @Test
    void updatesExistingNonApprovedEntry() {
        RawEntry raw = rawEntry(9L, "ha tin", "A1 win", "I win", Instant.parse("2026-03-25T12:00:00Z"));
        CorrectedEntry pending = correctedEntry(raw, false);
        pending.setRawText("old");
        pending.setGlossText("old");
        pending.setTranslationText("old");
        pending.setStale(true);

        when(correctedEntryRepository.findByRawEntryId(9L)).thenReturn(Optional.of(pending));
        when(pipelineFactory.createRuleEngine()).thenReturn(new RuleEngine(List.of()));
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorrectedEntry saved = service.applyToRawEntry(raw);

        assertThat(saved).isSameAs(pending);
        assertThat(saved.getRawText()).isEqualTo("ha tin");
        assertThat(saved.getGlossText()).isEqualTo("A1 win");
        assertThat(saved.getTranslationText()).isEqualTo("I win");
        assertThat(saved.getDescription()).contains("Generated from raw entry #9");
        assertThat(saved.isStale()).isFalse();
    }

    @Test
    void applyToAllReturnsProcessedCount() {
        RawEntry first = rawEntry(1L, "a", "b", "c", Instant.parse("2026-03-25T12:00:00Z"));
        RawEntry second = rawEntry(2L, "d", "e", "f", Instant.parse("2026-03-25T12:00:00Z"));

        when(correctedEntryRepository.findByRawEntryId(anyLong())).thenReturn(Optional.empty());
        when(pipelineFactory.createRuleEngine()).thenReturn(new RuleEngine(List.of()));
        when(correctedEntryRepository.save(any(CorrectedEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int count = service.applyToAll(List.of(first, second));

        assertThat(count).isEqualTo(2);
        verify(correctedEntryRepository, times(2)).save(any(CorrectedEntry.class));
    }

    private static RawEntry rawEntry(Long id, String rawText, String glossText, String translationText, Instant updatedAt) {
        RawEntry raw = new RawEntry();
        raw.setId(id);
        raw.setRawText(rawText);
        raw.setGlossText(glossText);
        raw.setTranslationText(translationText);
        raw.setUpdatedAt(updatedAt);
        return raw;
    }

    private static CorrectedEntry correctedEntry(RawEntry raw, Boolean approved) {
        CorrectedEntry corrected = new CorrectedEntry();
        corrected.setRawEntry(raw);
        corrected.setIsCorrect(approved);
        return corrected;
    }
}