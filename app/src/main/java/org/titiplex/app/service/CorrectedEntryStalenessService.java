package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CorrectedEntryStalenessService {
    private final CorrectedEntryRepository correctedEntryRepository;
    private final RulesetFingerprintService rulesetFingerprintService;

    public CorrectedEntryStalenessService(
            CorrectedEntryRepository correctedEntryRepository,
            RulesetFingerprintService rulesetFingerprintService
    ) {
        this.correctedEntryRepository = correctedEntryRepository;
        this.rulesetFingerprintService = rulesetFingerprintService;
    }

    public int markApprovedEntriesStaleIfRulesChanged() {
        List<CorrectedEntry> changed = new ArrayList<>();
        for (CorrectedEntry entry : correctedEntryRepository.findAllByIsCorrectTrue()) {
            if (rulesetFingerprintService.isCorrectionRulesetOutdated(entry) && !entry.isStaleDueToRules()) {
                entry.setStale(true);
                entry.setStaleDueToRules(true);
                changed.add(entry);
            }
        }

        if (!changed.isEmpty()) {
            correctedEntryRepository.saveAll(changed);
        }
        return changed.size();
    }
}
