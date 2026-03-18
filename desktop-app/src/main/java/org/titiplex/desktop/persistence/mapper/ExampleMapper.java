package org.titiplex.desktop.persistence.mapper;

import org.titiplex.desktop.domain.example.Example;
import org.titiplex.desktop.domain.example.ExampleSource;
import org.titiplex.desktop.persistence.entity.ExampleEntity;

public final class ExampleMapper {
    private ExampleMapper() {
    }

    public static Example toDomain(ExampleEntity entity) {
        return new Example(
                entity.getId(),
                entity.getExternalId(),
                entity.getSurfaceText(),
                entity.getNormalizedText(),
                entity.getGlossText(),
                entity.getTranslationText(),
                entity.getNotes(),
                new ExampleSource(entity.getSourceName(), entity.getSourceRef()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ExampleEntity toEntity(Example domain) {
        ExampleEntity entity = new ExampleEntity();
        entity.setId(domain.id());
        copyIntoEntity(domain, entity);
        return entity;
    }

    public static void copyIntoEntity(Example domain, ExampleEntity entity) {
        entity.setExternalId(domain.externalId());
        entity.setSurfaceText(domain.surfaceText());
        entity.setNormalizedText(domain.normalizedText());
        entity.setGlossText(domain.glossText());
        entity.setTranslationText(domain.translationText());
        entity.setNotes(domain.notes());
        entity.setSourceName(domain.source() == null ? null : domain.source().sourceName());
        entity.setSourceRef(domain.source() == null ? null : domain.source().sourceRef());
        entity.setStatus(domain.status());
    }
}
