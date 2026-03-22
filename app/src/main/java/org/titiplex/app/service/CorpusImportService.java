package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.persistence.entity.RawEntry;
import org.titiplex.app.persistence.repository.RawEntryRepository;
import org.titiplex.io.BlockReader;
import org.titiplex.io.DocxReader;
import org.titiplex.io.RawTextReader;
import org.titiplex.model.RawBlock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Transactional
public class CorpusImportService {
    private final RawEntryRepository rawEntryRepository;

    public CorpusImportService(RawEntryRepository rawEntryRepository) {
        this.rawEntryRepository = rawEntryRepository;
    }

    public int importFile(Path inputPath) throws IOException {
        BlockReader reader = readerFor(inputPath);

        try (InputStream in = Files.newInputStream(inputPath)) {
            List<RawBlock> blocks = reader.read(in);
            int count = 0;

            for (RawBlock block : blocks) {
                RawEntry entry = new RawEntry();
                entry.setRawText(block.chujText());
                entry.setGlossText(block.glossText());
                entry.setTranslationText(block.translation());
                entry.setDescription("Imported from " + inputPath.getFileName());
                rawEntryRepository.save(entry);
                count++;
            }

            return count;
        }
    }

    private BlockReader readerFor(Path inputPath) {
        String lower = inputPath.getFileName().toString().toLowerCase();
        if (lower.endsWith(".docx")) {
            return new DocxReader();
        }
        return new RawTextReader();
    }
}