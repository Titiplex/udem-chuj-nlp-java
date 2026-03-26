package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.align.TokenAligner;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.app.persistence.repository.RawEntryRepository;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.model.RawBlock;
import org.titiplex.pipeline.CorrectionPipeline;
import org.titiplex.rules.RuleEngine;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AutoCorrectionService {
    private final DesktopPipelineFactory pipelineFactory;
    private final CorrectedEntryRepository correctedEntryRepository;
    private final RawEntryRepository rawEntryRepository;
    private final RulesetFingerprintService rulesetFingerprintService;

    public AutoCorrectionService(
            DesktopPipelineFactory pipelineFactory,
            CorrectedEntryRepository correctedEntryRepository,
            RawEntryRepository rawEntryRepository,
            RulesetFingerprintService rulesetFingerprintService
    ) {
        this.pipelineFactory = pipelineFactory;
        this.correctedEntryRepository = correctedEntryRepository;
        this.rawEntryRepository = rawEntryRepository;
        this.rulesetFingerprintService = rulesetFingerprintService;
    }

    public CorrectedEntry applyToRawEntry(RawEntry rawEntry) {
        CorrectedEntry existing = rawEntry.getId() == null
                ? null
                : correctedEntryRepository.findByRawEntryId(rawEntry.getId()).orElse(null);

        if (existing != null && Boolean.TRUE.equals(existing.getIsCorrect())) {
            if (markApprovedEntryStaleIfNeeded(existing, rawEntry)) {
                correctedEntryRepository.save(existing);
            }
            return existing;
        }

        return recomputeIntoTarget(existing, rawEntry, false);
    }

    public CorrectedEntry regenerateDraftFromRaw(Long correctedEntryId) {
        CorrectedEntry existing = correctedEntryRepository.findById(correctedEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Corrected entry #" + correctedEntryId + " does not exist"));

        Long rawEntryId = existing.getRawEntry() == null ? null : existing.getRawEntry().getId();
        if (rawEntryId == null) {
            throw new IllegalStateException("Corrected entry #" + correctedEntryId + " is not linked to a raw entry");
        }

        RawEntry rawEntry = rawEntryRepository.findById(rawEntryId)
                .orElseThrow(() -> new IllegalStateException("Linked raw entry #" + rawEntryId + " does not exist"));

        return recomputeIntoTarget(existing, rawEntry, true);
    }

    public int applyToAll(List<RawEntry> rawEntries) {
        int count = 0;
        for (RawEntry rawEntry : rawEntries) {
            applyToRawEntry(rawEntry);
            count++;
        }
        return count;
    }

    private CorrectedEntry recomputeIntoTarget(CorrectedEntry existing, RawEntry rawEntry, boolean regenerateDraft) {
        RuleEngine engine = pipelineFactory.createRuleEngine();
        CorrectionPipeline pipeline = new CorrectionPipeline(new TokenAligner(), engine);

        int rawId = rawEntry.getId() == null ? 0 : rawEntry.getId().intValue();

        RawBlock rawBlock = new RawBlock(
                rawId,
                nullToEmpty(rawEntry.getRawText()),
                nullToEmpty(rawEntry.getGlossText()),
                nullToEmpty(rawEntry.getTranslationText())
        );

        CorrectedBlock corrected = pipeline.process(rawBlock);

        CorrectedEntry target = existing != null ? existing : new CorrectedEntry();

        target.setRawEntry(rawEntry);
        target.setRawText(corrected.chujText());
        target.setGlossText(corrected.glossText());
        target.setTranslationText(corrected.translation());
        target.setDescription((regenerateDraft ? "Regenerated draft" : "Generated") + " from raw entry #" + rawEntry.getId());

        target.setIsCorrect(false);
        target.setStale(false);
        target.setStaleDueToRaw(false);
        target.setStaleDueToRules(false);
        target.setApprovedRawUpdatedAt(null);
        target.setApprovedRulesFingerprint(null);

        return correctedEntryRepository.save(target);
    }

    private boolean markApprovedEntryStaleIfNeeded(CorrectedEntry existing, RawEntry rawEntry) {
        boolean changed = false;

        if (hasRawMovedSinceApproval(rawEntry.getUpdatedAt(), existing.getApprovedRawUpdatedAt())
                && !existing.isStaleDueToRaw()) {
            existing.setStaleDueToRaw(true);
            changed = true;
        }

        if (rulesetFingerprintService.isCorrectionRulesetOutdated(existing) && !existing.isStaleDueToRules()) {
            existing.setStaleDueToRules(true);
            changed = true;
        }

        if ((existing.isStaleDueToRaw() || existing.isStaleDueToRules()) && !existing.isStale()) {
            existing.setStale(true);
            changed = true;
        }

        return changed;
    }

    private static boolean hasRawMovedSinceApproval(Instant rawUpdatedAt, Instant approvedRawUpdatedAt) {
        if (approvedRawUpdatedAt == null) {
            return true;
        }
        return rawUpdatedAt != null && rawUpdatedAt.isAfter(approvedRawUpdatedAt);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
