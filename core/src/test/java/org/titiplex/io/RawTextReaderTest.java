package org.titiplex.io;

import org.junit.jupiter.api.Test;
import org.titiplex.model.RawBlock;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class RawTextReaderTest {

    @Test
    void readsInterlinearBlocksSeparatedByBlankLines() throws Exception {
        String text = String.join("\n",
                "1 ix naq",
                "A1 go",
                "I go",
                "",
                "2 ach winh",
                "DET man",
                "the man"
        );

        RawTextReader reader = new RawTextReader();
        List<RawBlock> blocks = reader.read(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

        assertEquals(2, blocks.size());
        assertEquals(1, blocks.get(0).id());
        assertEquals("1 ix naq", blocks.get(0).chujText());
        assertEquals("A1 go", blocks.get(0).glossText());
        assertEquals("I go", blocks.get(0).translation());
    }

    @Test
    void mergesExtraTranslationLinesIntoSingleTranslationField() throws Exception {
        String text = String.join("\n",
                "ix naq",
                "A1 go",
                "I go",
                "today",
                ""
        );

        RawTextReader reader = new RawTextReader();
        List<RawBlock> blocks = reader.read(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, blocks.size());
        assertEquals("I go today", blocks.get(0).translation());
    }
}
