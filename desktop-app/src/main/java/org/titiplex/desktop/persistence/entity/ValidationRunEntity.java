package org.titiplex.desktop.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "validation_runs")
public class ValidationRunEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(nullable = false)
    private boolean ok;

    @Column(columnDefinition = "CLOB")
    private String summary;

    @OneToMany(mappedBy = "validationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValidationMessageEntity> messages = new ArrayList<>();

    public ValidationRunEntity() {
    }

    @PrePersist
    public void prePersist() {
        this.startedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<ValidationMessageEntity> getMessages() {
        return messages;
    }
}
