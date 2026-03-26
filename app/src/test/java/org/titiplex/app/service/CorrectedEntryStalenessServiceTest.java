package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrectedEntryStalenessServiceTest {

    @Mock
    private CorrectedEntryRepository correctedEntryRepository;

    @Mock
    private RulesetFingerprintService rulesetFingerprintService;

    @InjectMocks
    private CorrectedEntryStalenessService service;

    @Test
    void marksOnlyOutdatedApprovedEntriesAsStale() {
        CorrectedEntry fresh = new CorrectedEntry();
        fresh.setId(1L);
        fresh.setIsCorrect(true);
        fresh.setStale(false);

        CorrectedEntry outdated = new CorrectedEntry();
        outdated.setId(2L);
        outdated.setIsCorrect(true);
        outdated.setStale(false);

        when(correctedEntryRepository.findAllByIsCorrectTrue()).thenReturn(List.of(fresh, outdated));
        when(rulesetFingerprintService.isCorrectionRulesetOutdated(fresh)).thenReturn(false);
        when(rulesetFingerprintService.isCorrectionRulesetOutdated(outdated)).thenReturn(true);

        int count = service.markApprovedEntriesStaleIfRulesChanged();

        assertEquals(1, count);
        verify(correctedEntryRepository).saveAll(List.of(outdated));
    }
}
