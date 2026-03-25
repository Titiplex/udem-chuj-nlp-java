package org.titiplex.app.ui.conllu;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.service.AnnotationConfigStateService;
import org.titiplex.app.service.AppRefreshCoordinator;
import org.titiplex.app.service.ConlluPreviewService;
import org.titiplex.app.service.CorrectedEntryService;
import org.titiplex.conllu.AnnotationConfig;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ConlluPanelTest {

    @Test
    void refreshPreservesSelectedEntryAndPreviewWhenEntryStillExists() throws Exception {
        CorrectedEntryService correctedEntryService = Mockito.mock(CorrectedEntryService.class);
        AnnotationConfigStateService annotationConfigStateService = Mockito.mock(AnnotationConfigStateService.class);
        ConlluPreviewService conlluPreviewService = Mockito.mock(ConlluPreviewService.class);

        CorrectedEntry first = entry(1L, "one");
        CorrectedEntry second = entry(2L, "two");

        AtomicReference<List<CorrectedEntry>> entries = new AtomicReference<>(List.of(first, second));
        when(correctedEntryService.getAll()).thenAnswer(invocation -> entries.get());
        when(annotationConfigStateService.getCurrentConfig()).thenReturn(new AnnotationConfig());
        when(conlluPreviewService.preview(any(CorrectedEntry.class), any(AnnotationConfig.class)))
                .thenAnswer(invocation -> "preview#" + ((CorrectedEntry) invocation.getArgument(0)).getId());

        Consumer<String> status = s -> {
        };
        AppRefreshCoordinator coordinator = new AppRefreshCoordinator();

        ConlluPanel[] holder = new ConlluPanel[1];
        SwingUtilities.invokeAndWait(() ->
                holder[0] = new ConlluPanel(
                        correctedEntryService,
                        annotationConfigStateService,
                        conlluPreviewService,
                        coordinator,
                        status
                )
        );

        ConlluPanel panel = holder[0];
        JTable table = getField(panel, "table", JTable.class);
        JTextArea previewArea = getField(panel, "previewArea", JTextArea.class);

        SwingUtilities.invokeAndWait(() -> table.setRowSelectionInterval(1, 1));
        assertEquals("preview#2", previewArea.getText());

        entries.set(List.of(second, first));
        SwingUtilities.invokeAndWait(panel::refresh);

        assertEquals(0, table.getSelectedRow());
        assertEquals("preview#2", previewArea.getText());
    }

    private static CorrectedEntry entry(Long id, String translation) {
        CorrectedEntry entry = new CorrectedEntry();
        entry.setId(id);
        entry.setTranslationText(translation);
        entry.setIsCorrect(false);
        return entry;
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}