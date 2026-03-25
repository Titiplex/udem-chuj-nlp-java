package org.titiplex.app.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}