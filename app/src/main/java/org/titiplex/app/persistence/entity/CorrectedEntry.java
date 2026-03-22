package org.titiplex.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "corrected_entries")
public class CorrectedEntry extends Entry {
    @Column(name = "raw_entry_id")
    private Long rawEntryId;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}
