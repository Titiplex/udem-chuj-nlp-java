package org.titiplex.app.ui.conllu;

import org.junit.jupiter.api.Test;
import org.titiplex.app.persistence.entity.CorrectedEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConlluEntrySelectionTableModelTest {

    @Test
    void exposesWorkflowStatusAndStaleReason() {
        ConlluEntrySelectionTableModel model = new ConlluEntrySelectionTableModel();

        CorrectedEntry entry = new CorrectedEntry();
        entry.setId(5L);
        entry.setTranslationText("I win");
        entry.setIsCorrect(true);
        entry.setStale(true);
        entry.setStaleDueToRules(true);

        model.setEntries(List.of(entry));

        assertEquals("Approved (stale)", model.getValueAt(0, 2));
        assertEquals("Rules changed", model.getValueAt(0, 3));
    }
}