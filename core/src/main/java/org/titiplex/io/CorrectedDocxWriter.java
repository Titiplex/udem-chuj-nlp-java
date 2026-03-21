package org.titiplex.io;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.titiplex.model.CorrectionEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CorrectedDocxWriter {
    private static final int MAX_CHARS_PER_LINE = 60;

    public void write(Path output, List<CorrectionEntry> entries) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            for (CorrectionEntry entry : entries) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setBold(true);
                run.setText(entry.id() + ".");
                run.addBreak();

                for (String line : wrapAligned(entry.corrected().chujText(), entry.corrected().glossText())) {
                    run.setText("\t" + line);
                    run.addBreak();
                }
                if (!entry.corrected().translation().isBlank()) {
                    run.setText("\t" + entry.corrected().translation());
                    run.addBreak();
                }
            }
            try (OutputStream out = Files.newOutputStream(output)) {
                document.write(out);
            }
        }
    }

    private List<String> wrapAligned(String chuj, String gloss) {
        List<String> out = new ArrayList<>();
        List<String> chujWords = splitWords(chuj);
        List<String> glossWords = splitWords(gloss);

        int i = 0;
        while (i < Math.max(chujWords.size(), glossWords.size())) {
            StringBuilder chujLine = new StringBuilder();
            StringBuilder glossLine = new StringBuilder();
            int width = 0;
            while (i < Math.max(chujWords.size(), glossWords.size())) {
                String cw = i < chujWords.size() ? chujWords.get(i) : "_";
                String gw = i < glossWords.size() ? glossWords.get(i) : "_";
                int colWidth = Math.max(cw.length(), gw.length()) + 2;
                if (width > 0 && width + colWidth > MAX_CHARS_PER_LINE) break;
                appendPadded(chujLine, cw, colWidth);
                appendPadded(glossLine, gw, colWidth);
                width += colWidth;
                i++;
            }
            out.add(rtrim(chujLine.toString()));
            out.add(rtrim(glossLine.toString()));
        }
        return out;
    }

    private static List<String> splitWords(String line) {
        if (line == null || line.isBlank()) return List.of();
        return List.of(line.trim().split("\\s+"));
    }

    private static void appendPadded(StringBuilder sb, String value, int width) {
        sb.append(value);
        int pad = Math.max(1, width - value.length());
        sb.append(" ".repeat(pad));
    }

    private static String rtrim(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) end--;
        return s.substring(0, end);
    }
}
