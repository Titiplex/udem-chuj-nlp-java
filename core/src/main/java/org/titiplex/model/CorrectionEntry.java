package org.titiplex.model;

public record CorrectionEntry(
        int id,
        RawBlock initial,
        CorrectedBlock corrected
) {
}
