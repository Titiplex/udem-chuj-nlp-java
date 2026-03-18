package org.titiplex.desktop.persistence.mapper;

import org.titiplex.desktop.domain.rule.Rule;
import org.titiplex.desktop.domain.rule.RuleId;
import org.titiplex.desktop.domain.rule.RuleVersion;
import org.titiplex.desktop.persistence.entity.RuleEntity;

public final class RuleMapper {
    private RuleMapper() {
    }

    public static Rule toDomain(RuleEntity entity) {
        return new Rule(
                entity.getId(),
                new RuleId(entity.getStableId()),
                entity.getName(),
                entity.isEnabled(),
                entity.getYamlBody(),
                entity.getSourceFile(),
                entity.getDescription(),
                new RuleVersion(entity.getVersionNo()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static RuleEntity toEntity(Rule domain) {
        RuleEntity entity = new RuleEntity();
        entity.setId(domain.id());
        entity.setStableId(domain.ruleId().value());
        entity.setName(domain.name());
        entity.setEnabled(domain.enabled());
        entity.setYamlBody(domain.yamlBody());
        entity.setSourceFile(domain.sourceFile());
        entity.setDescription(domain.description());
        entity.setVersionNo(domain.version().value());
        return entity;
    }

    public static void copyIntoEntity(Rule domain, RuleEntity entity) {
        entity.setStableId(domain.ruleId().value());
        entity.setName(domain.name());
        entity.setEnabled(domain.enabled());
        entity.setYamlBody(domain.yamlBody());
        entity.setSourceFile(domain.sourceFile());
        entity.setDescription(domain.description());
        entity.setVersionNo(domain.version().value());
    }
}
