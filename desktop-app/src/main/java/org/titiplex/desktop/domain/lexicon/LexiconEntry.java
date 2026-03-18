package org.titiplex.desktop.domain.lexicon;

public record LexiconEntry(
        Long id,
        String lexiconName,
        String entryKey,
        String entryValue,
        String metadataJson
) {
}
