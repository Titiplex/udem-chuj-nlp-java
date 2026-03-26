package org.titiplex.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
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
    void marksApprovedEntriesStaleDueToRulesWhenFingerprintIsOutdated() {
        CorrectedEntry current = new CorrectedEntry();
        current.setId(1L);
        current.setIsCorrect(true);
        current.setStale(false);

        CorrectedEntry alreadyRawStale = new CorrectedEntry();
        alreadyRawStale.setId(2L);
        alreadyRawStale.setIsCorrect(true);
        alreadyRawStale.setStale(true);
        alreadyRawStale.setStaleDueToRaw(true);

        when(correctedEntryRepository.findAllByIsCorrectTrue()).thenReturn(List.of(current, alreadyRawStale));
        when(rulesetFingerprintService.isCorrectionRulesetOutdated(current)).thenReturn(true);
        when(rulesetFingerprintService.isCorrectionRulesetOutdated(alreadyRawStale)).thenReturn(true);

        int changed = service.markApprovedEntriesStaleIfRulesChanged();

        assertEquals(2, changed);
        verify(correctedEntryRepository).saveAll(argThat((ArgumentMatcher<List<CorrectedEntry>>) list ->
                list.size() == 2
                        && list.stream().allMatch(CorrectedEntry::isStale)
                        && list.stream().allMatch(CorrectedEntry::isStaleDueToRules)
        ));
    }
}
