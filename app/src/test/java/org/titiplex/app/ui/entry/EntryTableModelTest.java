package org.titiplex.app.ui.entry;

import org.junit.jupiter.api.Test;
import org.titiplex.app.persistence.entity.CorrectedEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntryTableModelTest {

    @Test
    void exposesWorkflowStatusAndStaleReason() {
        EntryTableModel model = new EntryTableModel();

        CorrectedEntry entry = new CorrectedEntry();
        entry.setId(12L);
        entry.setIsCorrect(true);
        entry.setStale(true);
        entry.setStaleDueToRaw(true);
        entry.setStaleDueToRules(true);

        model.setEntries(List.of(entry));

        assertEquals("Approved (stale)", model.getValueAt(0, 1));
        assertEquals("Raw + rules changed", model.getValueAt(0, 2));
    }
}
