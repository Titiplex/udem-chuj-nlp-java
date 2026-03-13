package org.titiplex.io;

import org.titiplex.model.ConlluSentence;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public final class ConlluWriter {
    public void write(List<ConlluSentence> sentences, Writer writer) throws IOException {
        for (ConlluSentence sentence : sentences) {
            writer.write("# sent_id = " + sentence.sentId() + System.lineSeparator());
            writer.write("# text = " + sentence.text() + System.lineSeparator());
            for (var token : sentence.tokens()) {
                writer.write(token.toConlluLine() + System.lineSeparator());
            }
            writer.write(System.lineSeparator());
        }
        writer.flush();
    }
}
