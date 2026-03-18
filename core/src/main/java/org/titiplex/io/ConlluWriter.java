package org.titiplex.io;

import org.titiplex.conllu.ConlluEntry;
import org.titiplex.model.ConlluSentence;
import org.titiplex.model.ConlluToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConlluWriter {
    public void writeSentence(Path out, ConlluSentence sentence) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# sent_id = ").append(sentence.sentId()).append("\n");
        sb.append("# text = ").append(sentence.text()).append("\n");
        for (ConlluToken token : sentence.tokens()) sb.append(token.toConlluLine()).append("\n");
        sb.append("\n");
        Files.writeString(out, sb.toString(), StandardCharsets.UTF_8);
    }

    public void writeEntry(Path out, ConlluEntry entry) throws IOException {
        Files.writeString(out, entry.toConlluString(), StandardCharsets.UTF_8);
    }

    public void writeRaw(Path out, String text) throws IOException {
        Files.writeString(out, text, StandardCharsets.UTF_8);
    }
}
