package org.titiplex.desktop.persistence.mapper;

import org.titiplex.desktop.domain.correction.Correction;
import org.titiplex.desktop.persistence.entity.CorrectionEntity;
import org.titiplex.desktop.persistence.entity.ExampleEntity;
import org.titiplex.desktop.persistence.entity.RuleEntity;

public final class CorrectionMapper {
    private CorrectionMapper() {
    }

    public static Correction toDomain(CorrectionEntity entity) {
        return new Correction(
                entity.getId(),
                entity.getExample().getId(),
                entity.getRule() == null ? null : entity.getRule().getId(),
                entity.getBeforePayload(),
                entity.getAfterPayload(),
                entity.getDecision(),
                entity.getOrigin(),
                entity.getComment(),
                entity.getCreatedAt()
        );
    }

    public static CorrectionEntity toEntity(Correction domain, ExampleEntity exampleEntity, RuleEntity ruleEntity) {
        CorrectionEntity entity = new CorrectionEntity();
        entity.setExample(exampleEntity);
        entity.setRule(ruleEntity);
        entity.setBeforePayload(domain.beforePayload());
        entity.setAfterPayload(domain.afterPayload());
        entity.setDecision(domain.decision());
        entity.setOrigin(domain.origin());
        entity.setComment(domain.comment());
        return entity;
    }
}
