package org.titiplex.io;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.titiplex.model.RawBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class DocxReader implements BlockReader {

    @Override
    public List<RawBlock> read(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<String> nonBlankLines = new ArrayList<>();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = InterlinearBlockParser.normalize(paragraph.getText());
                if (!text.isBlank()) {
                    nonBlankLines.add(text);
                }
            }
            return InterlinearBlockParser.parseNumberedLines(nonBlankLines);
        }
    }
}
