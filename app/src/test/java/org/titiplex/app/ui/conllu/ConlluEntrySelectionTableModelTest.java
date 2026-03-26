package org.titiplex.app.ui.conllu;

import org.junit.jupiter.api.Test;
import org.titiplex.app.persistence.entity.CorrectedEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConlluEntrySelectionTableModelTest {

    @Test
    void showsWorkflowStatusInsteadOfBareApprovedBoolean() {
        ConlluEntrySelectionTableModel model = new ConlluEntrySelectionTableModel();

        CorrectedEntry approvedCurrent = new CorrectedEntry();
        approvedCurrent.setId(1L);
        approvedCurrent.setTranslationText("I win");
        approvedCurrent.setIsCorrect(true);
        approvedCurrent.setStale(false);

        model.setEntries(List.of(approvedCurrent));

        assertEquals("Status", model.getColumnName(2));
        assertEquals("Approved", model.getValueAt(0, 2));
    }
}
