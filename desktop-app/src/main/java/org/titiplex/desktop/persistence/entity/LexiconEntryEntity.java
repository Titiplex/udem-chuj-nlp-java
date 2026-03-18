package org.titiplex.desktop.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lexicon_entries")
public class LexiconEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lexicon_name", nullable = false, length = 120)
    private String lexiconName;

    @Column(name = "entry_key", nullable = false, length = 255)
    private String entryKey;

    @Column(name = "entry_value", nullable = false, columnDefinition = "CLOB")
    private String entryValue;

    @Column(name = "metadata_json", columnDefinition = "CLOB")
    private String metadataJson;

    public LexiconEntryEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getLexiconName() {
        return lexiconName;
    }

    public void setLexiconName(String lexiconName) {
        this.lexiconName = lexiconName;
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }
}
