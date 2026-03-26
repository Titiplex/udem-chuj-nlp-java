package org.titiplex.app.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "corrected_entries")
public class CorrectedEntry extends Entry {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_entry_id", unique = true)
    private RawEntry rawEntry;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "stale", nullable = false)
    private boolean stale = false;

    @Column(name = "approved_raw_updated_at")
    private Instant approvedRawUpdatedAt;

    public String workflowStatusLabel() {
        if (Boolean.TRUE.equals(isCorrect)) {
            return stale ? "Approved (stale)" : "Approved";
        }
        return "Draft";
    }
}