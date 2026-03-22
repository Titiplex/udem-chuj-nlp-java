package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.titiplex.align.TokenAligner;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.repository.CorrectedEntryRepository;
import org.titiplex.io.CorrectedDocxWriter;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.model.CorrectionEntry;
import org.titiplex.model.RawBlock;
import org.titiplex.stats.CorpusStats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DesktopExportService {
    private final CorrectedEntryRepository correctedEntryRepository;

    public DesktopExportService(CorrectedEntryRepository correctedEntryRepository) {
        this.correctedEntryRepository = correctedEntryRepository;
    }

    public void exportCorrectedDocx(Path outputPath) throws IOException {
        List<CorrectionEntry> entries = toCorrectionEntries(correctedEntryRepository.findAll());
        new CorrectedDocxWriter().write(outputPath, entries);
    }

    public void exportStats(Path outputPath) throws IOException {
        CorpusStats stats = new CorpusStats();
        for (CorrectedEntry entry : correctedEntryRepository.findAll()) {
            stats.accept(toCorrectedBlock(entry));
        }
        Files.writeString(outputPath, stats.toReportString(), StandardCharsets.UTF_8);
    }

    private List<CorrectionEntry> toCorrectionEntries(List<CorrectedEntry> entities) {
        List<CorrectionEntry> out = new ArrayList<>();
        for (CorrectedEntry entity : entities) {
            int id = entity.getId() == null ? 0 : entity.getId().intValue();
            RawBlock initial = new RawBlock(
                    id,
                    nullToEmpty(entity.getRawText()),
                    nullToEmpty(entity.getGlossText()),
                    nullToEmpty(entity.getTranslationText())
            );
            out.add(new CorrectionEntry(id, initial, toCorrectedBlock(entity)));
        }
        return out;
    }

    private CorrectedBlock toCorrectedBlock(CorrectedEntry entity) {
        int id = entity.getId() == null ? 0 : entity.getId().intValue();
        String chuj = nullToEmpty(entity.getRawText());
        String gloss = nullToEmpty(entity.getGlossText());
        String translation = nullToEmpty(entity.getTranslationText());

        return new CorrectedBlock(
                id,
                chuj,
                gloss,
                translation,
                new TokenAligner().align(Collections.singletonList(chuj), Collections.singletonList(gloss))
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}