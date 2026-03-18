package org.titiplex.desktop.persistence.entity;

import jakarta.persistence.*;
import org.titiplex.desktop.domain.example.ExampleStatus;

import java.time.Instant;

@Entity
@Table(name = "examples")
public class ExampleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", length = 120)
    private String externalId;

    @Column(name = "surface_text", nullable = false, columnDefinition = "CLOB")
    private String surfaceText;

    @Column(name = "normalized_text", columnDefinition = "CLOB")
    private String normalizedText;

    @Column(name = "gloss_text", columnDefinition = "CLOB")
    private String glossText;

    @Column(name = "translation_text", columnDefinition = "CLOB")
    private String translationText;

    @Column(columnDefinition = "CLOB")
    private String notes;

    @Column(name = "source_name", length = 255)
    private String sourceName;

    @Column(name = "source_ref", length = 255)
    private String sourceRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ExampleStatus status = ExampleStatus.RAW;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ExampleEntity() {
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getSurfaceText() {
        return surfaceText;
    }

    public void setSurfaceText(String surfaceText) {
        this.surfaceText = surfaceText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public void setNormalizedText(String normalizedText) {
        this.normalizedText = normalizedText;
    }

    public String getGlossText() {
        return glossText;
    }

    public void setGlossText(String glossText) {
        this.glossText = glossText;
    }

    public String getTranslationText() {
        return translationText;
    }

    public void setTranslationText(String translationText) {
        this.translationText = translationText;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public ExampleStatus getStatus() {
        return status;
    }

    public void setStatus(ExampleStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
