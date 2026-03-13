package org.titiplex.io;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.titiplex.model.RawBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class DocxReader {
    public List<RawBlock> read(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<String> nonBlankLines = new ArrayList<>();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    nonBlankLines.add(text.trim());
                }
            }

            List<RawBlock> blocks = new ArrayList<>();
            int id = 1;
            for (int i = 0; i + 2 < nonBlankLines.size(); i += 3) {
                blocks.add(new RawBlock(id++, nonBlankLines.get(i), nonBlankLines.get(i + 1), nonBlankLines.get(i + 2)));
            }
            return blocks;
        }
    }
}
