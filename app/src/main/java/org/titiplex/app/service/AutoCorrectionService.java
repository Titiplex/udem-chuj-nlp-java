package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.align.TokenAligner;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.model.RawBlock;
import org.titiplex.pipeline.CorrectionPipeline;
import org.titiplex.rules.RuleEngine;

import java.util.List;

@Service
@Transactional
public class AutoCorrectionService {
    private final DesktopPipelineFactory pipelineFactory;
    private final CorrectedEntryRepository correctedEntryRepository;

    public AutoCorrectionService(
            DesktopPipelineFactory pipelineFactory,
            CorrectedEntryRepository correctedEntryRepository
    ) {
        this.pipelineFactory = pipelineFactory;
        this.correctedEntryRepository = correctedEntryRepository;
    }

    public CorrectedEntry applyToRawEntry(RawEntry rawEntry) {
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

        CorrectedEntry target = rawEntry.getId() == null
                ? new CorrectedEntry()
                : correctedEntryRepository.findByRawEntryId(rawEntry.getId()).orElseGet(CorrectedEntry::new);

        target.setRawEntry(rawEntry);
        target.setRawText(corrected.chujText());
        target.setGlossText(corrected.glossText());
        target.setTranslationText(corrected.translation());
        target.setDescription("Generated from raw entry #" + rawEntry.getId());
        if (target.getIsCorrect() == null) {
            target.setIsCorrect(false);
        }

        return correctedEntryRepository.save(target);
    }

    public int applyToAll(List<RawEntry> rawEntries) {
        int count = 0;
        for (RawEntry rawEntry : rawEntries) {
            applyToRawEntry(rawEntry);
            count++;
        }
        return count;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}