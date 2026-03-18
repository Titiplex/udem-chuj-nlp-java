package org.titiplex.desktop.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "rules")
public class RuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stable_id", nullable = false, length = 200, unique = true)
    private String stableId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "yaml_body", nullable = false, columnDefinition = "CLOB")
    private String yamlBody;

    @Column(name = "source_file", length = 255)
    private String sourceFile;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(name = "version_no", nullable = false)
    private int versionNo = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RuleEntity() {
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

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getYamlBody() {
        return yamlBody;
    }

    public void setYamlBody(String yamlBody) {
        this.yamlBody = yamlBody;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
