package org.titiplex.desktop.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "validation_messages")
public class ValidationMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validation_run_id", nullable = false)
    private ValidationRunEntity validationRun;

    @Column(nullable = false, length = 16)
    private String severity;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String message;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "example_id")
    private Long exampleId;

    public ValidationMessageEntity() {
    }

    public Long getId() {
        return id;
    }

    public ValidationRunEntity getValidationRun() {
        return validationRun;
    }

    public void setValidationRun(ValidationRunEntity validationRun) {
        this.validationRun = validationRun;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }
}
