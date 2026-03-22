package org.titiplex.app.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.titiplex.app.persistence.entity.CorrectedEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class PdfExportService {

    public void exportCorrectedEntries(List<CorrectedEntry> entries, Path output) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                content.setLeading(14.5f);
                content.newLineAtOffset(50, 750);

                for (CorrectedEntry entry : entries) {
                    writeLine(content, "ID: " + entry.getId());
                    writeLine(content, "Text: " + safe(entry.getRawText()));
                    writeLine(content, "Gloss: " + safe(entry.getGlossText()));
                    writeLine(content, "Translation: " + safe(entry.getTranslationText()));
                    writeLine(content, "-----");
                }

                content.endText();
            }

            document.save(output.toFile());
        }
    }

    private void writeLine(PDPageContentStream content, String line) throws IOException {
        content.showText(line == null ? "" : line);
        content.newLine();
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\n", " ");
    }
}