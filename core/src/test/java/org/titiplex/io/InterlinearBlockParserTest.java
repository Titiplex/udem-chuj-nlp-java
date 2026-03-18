package org.titiplex.io;

import org.junit.jupiter.api.Test;
import org.titiplex.model.RawBlock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class InterlinearBlockParserTest {

    @Test
    void parsesMultilineExampleWithoutShiftingFollowingExamples() {
        List<String> lines = List.of(
                "30\tay-ø jun xo ix,",
                "exist-B3- uno modf Pro3,",
                "ha ix ix y-et' b'ey-um winh y-et'-nak winh in-nulej chi'",
                "top Clsf mujer A3-SR caminar-agtvz Pro3 A3-SR-ppf Clsf A1-hermano dst",
                "'hay otra mujer, que es la esposa del compañero de mi hermano'",
                "31\ta ix ø-pax w-et'-nak a in tik",
                "top Pro3 B3-regresar A1-SR-ppf top B1 prx",
                "'ella es la que se regresa conmigo'"
        );

        List<RawBlock> blocks = InterlinearBlockParser.parseNumberedLines(lines);
        assertEquals(2, blocks.size());

        RawBlock b30 = blocks.get(0);
        assertEquals(30, b30.id());
        assertEquals("30 ay-ø jun xo ix, ha ix ix y-et' b'ey-um winh y-et'-nak winh in-nulej chi'", b30.chujText());
        assertEquals("exist-B3- uno modf Pro3, top Clsf mujer A3-SR caminar-agtvz Pro3 A3-SR-ppf Clsf A1-hermano dst", b30.glossText());
        assertEquals("hay otra mujer, que es la esposa del compañero de mi hermano", b30.translation());

        RawBlock b31 = blocks.get(1);
        assertEquals(31, b31.id());
        assertEquals("31 a ix ø-pax w-et'-nak a in tik", b31.chujText());
        assertEquals("top Pro3 B3-regresar A1-SR-ppf top B1 prx", b31.glossText());
        assertEquals("ella es la que se regresa conmigo", b31.translation());
    }
}
