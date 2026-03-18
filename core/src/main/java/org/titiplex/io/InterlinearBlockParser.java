package org.titiplex.io;

import org.titiplex.model.RawBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses interlinear examples from DOCX/plain-text sources.
 *
 * Expected robust shape:
 *   n<TAB>CHUJ PART 1
 *   GLOSS PART 1
 *   [optional CHUJ PART 2]
 *   [optional GLOSS PART 2]
 *   'translation'
 *
 * The previous reader consumed non-blank lines in fixed groups of three, which
 * breaks as soon as an example spans multiple Chuj/gloss lines.
 */
public final class InterlinearBlockParser {
    private static final Pattern NUMBERED_START = Pattern.compile("^\\d+\\s+.+$");

    private InterlinearBlockParser() {
    }

    public static List<RawBlock> parseNumberedLines(List<String> rawLines) {
        List<String> lines = new ArrayList<>();
        for (String raw : rawLines) {
            String line = normalize(raw);
            if (!line.isBlank()) {
                lines.add(line);
            }
        }

        List<List<String>> grouped = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            if (isNumberedStart(line)) {
                if (!current.isEmpty()) {
                    grouped.add(List.copyOf(current));
                    current.clear();
                }
            }
            current.add(line);
        }
        if (!current.isEmpty()) {
            grouped.add(List.copyOf(current));
        }

        List<RawBlock> out = new ArrayList<>();
        int fallbackId = 1;
        for (List<String> group : grouped) {
            out.add(toBlock(group, fallbackId++));
        }
        return out;
    }

    public static boolean isNumberedStart(String line) {
        return line != null && NUMBERED_START.matcher(line).matches();
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw
                .replace('\u2019', '\'')
                .replace('\u2018', '\'')
                .replace('\u02BC', '\'')
                .replace('\u201B', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .replace('\u00A0', ' ')
                .replace('\t', ' ')
                .trim();
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    private static RawBlock toBlock(List<String> group, int fallbackId) {
        if (group.isEmpty()) {
            return new RawBlock(fallbackId, "", "", "");
        }

        int id = extractLeadingId(group.getFirst(), fallbackId);
        List<String> chujParts = new ArrayList<>();
        List<String> glossParts = new ArrayList<>();
        List<String> translationParts = new ArrayList<>();

        chujParts.add(group.getFirst());
        boolean expectGloss = true;
        for (int i = 1; i < group.size(); i++) {
            String line = group.get(i);
            if (!translationParts.isEmpty() || looksLikeTranslation(line)) {
                translationParts.add(stripOuterQuotes(line));
                continue;
            }
            if (expectGloss) {
                glossParts.add(line);
            } else {
                chujParts.add(line);
            }
            expectGloss = !expectGloss;
        }

        // Fallback when the source has no quotes around the translation.
        if (translationParts.isEmpty() && !glossParts.isEmpty() && glossParts.size() > chujParts.size()) {
            translationParts.add(stripOuterQuotes(glossParts.removeLast()));
        }

        return new RawBlock(
                id,
                String.join(" ", chujParts).trim(),
                String.join(" ", glossParts).trim(),
                String.join(" ", translationParts).trim()
        );
    }

    private static boolean looksLikeTranslation(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        String trimmed = line.trim();
        return trimmed.startsWith("'")
                || trimmed.startsWith("‘")
                || trimmed.startsWith("\"")
                || trimmed.startsWith("“");
    }

    private static String stripOuterQuotes(String line) {
        String s = normalize(line);
        if (s.length() >= 2) {
            if ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))) {
                return s.substring(1, s.length() - 1).trim();
            }
        }
        return s;
    }

    private static int extractLeadingId(String line, int fallbackId) {
        if (line == null) {
            return fallbackId;
        }
        int i = 0;
        while (i < line.length() && Character.isDigit(line.charAt(i))) {
            i++;
        }
        if (i == 0) {
            return fallbackId;
        }
        try {
            return Integer.parseInt(line.substring(0, i));
        } catch (NumberFormatException e) {
            return fallbackId;
        }
    }
}
