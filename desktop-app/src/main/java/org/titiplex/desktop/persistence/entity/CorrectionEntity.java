package org.titiplex.desktop.persistence.entity;

import jakarta.persistence.*;
import org.titiplex.desktop.domain.correction.CorrectionDecision;
import org.titiplex.desktop.domain.correction.CorrectionOrigin;

import java.time.Instant;

@Entity
@Table(name = "corrections")
public class CorrectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "example_id", nullable = false)
    private ExampleEntity example;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;

    @Column(name = "before_payload", columnDefinition = "CLOB")
    private String beforePayload;

    @Column(name = "after_payload", columnDefinition = "CLOB")
    private String afterPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CorrectionDecision decision = CorrectionDecision.PROPOSED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CorrectionOrigin origin = CorrectionOrigin.RULE_ENGINE;

    @Column(columnDefinition = "CLOB")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CorrectionEntity() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public ExampleEntity getExample() {
        return example;
    }

    public void setExample(ExampleEntity example) {
        this.example = example;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public void setRule(RuleEntity rule) {
        this.rule = rule;
    }

    public String getBeforePayload() {
        return beforePayload;
    }

    public void setBeforePayload(String beforePayload) {
        this.beforePayload = beforePayload;
    }

    public String getAfterPayload() {
        return afterPayload;
    }

    public void setAfterPayload(String afterPayload) {
        this.afterPayload = afterPayload;
    }

    public CorrectionDecision getDecision() {
        return decision;
    }

    public void setDecision(CorrectionDecision decision) {
        this.decision = decision;
    }

    public CorrectionOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(CorrectionOrigin origin) {
        this.origin = origin;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
