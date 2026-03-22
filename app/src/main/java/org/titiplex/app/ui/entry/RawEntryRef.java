package org.titiplex.app.ui.entry;

public record RawEntryRef(Long id, String label) {
    @Override
    public String toString() {
        return label;
    }
}