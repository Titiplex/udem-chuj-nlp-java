package org.titiplex.pipeline;

import org.junit.jupiter.api.Test;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.ConlluEntry;
import org.titiplex.model.AlignedToken;
import org.titiplex.model.ConlluSentence;
import org.titiplex.model.CorrectedBlock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class ConlluPipelineTest {

    @Test
    void toEntryBuildsHeadersAndLines() {
        AnnotationConfig config = new AnnotationConfig();
        config.glossMapper().putPos("noun", "NOUN");

        CorrectedBlock block = new CorrectedBlock(
                3,
                "winh",
                "noun",
                "man",
                List.of(AlignedToken.of("winh", "noun", List.of("winh"), List.of("noun")))
        );

        ConlluPipeline pipeline = new ConlluPipeline(config);
        ConlluEntry entry = pipeline.toEntry(block);

        assertFalse(entry.headers().isEmpty());
        assertTrue(entry.toConlluString().contains("# sent_id = 3"));
        assertTrue(entry.toConlluString().contains("# text = winh"));
        assertTrue(entry.toConlluString().contains("# translation = man"));
        assertEquals(1, entry.lines().size());
    }

    @Test
    void toConlluReturnsSentenceModel() {
        AnnotationConfig config = new AnnotationConfig();
        config.glossMapper().putPos("noun", "NOUN");

        CorrectedBlock block = new CorrectedBlock(
                4,
                "winh",
                "noun",
                "man",
                List.of(AlignedToken.of("winh", "noun", List.of("winh"), List.of("noun")))
        );

        ConlluPipeline pipeline = new ConlluPipeline(config);
        ConlluSentence sentence = pipeline.toConllu(block);

        assertEquals("4", sentence.sentId());
        assertEquals("winh", sentence.text());
        assertEquals(1, sentence.tokens().size());
        assertEquals("NOUN", sentence.tokens().get(0).upos());
    }
}
