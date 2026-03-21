package org.titiplex.app.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "conllu_outputs")
@Getter
@Setter
@Data
public class ConlluOutput {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
