package org.titiplex.app.ui.entry;

import org.junit.jupiter.api.Test;
import org.titiplex.app.persistence.entity.CorrectedEntry;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntryTableModelTest {

    @Test
    void showsWorkflowStatusInStatusColumn() {
        EntryTableModel model = new EntryTableModel();

        CorrectedEntry approvedStale = new CorrectedEntry();
        approvedStale.setId(1L);
        approvedStale.setIsCorrect(true);
        approvedStale.setStale(true);
        approvedStale.setUpdatedAt(Instant.parse("2026-03-25T12:00:00Z"));

        model.setEntries(List.of(approvedStale));

        assertEquals("Status", model.getColumnName(1));
        assertEquals("Approved (stale)", model.getValueAt(0, 1));
    }
}
