package org.titiplex.desktop.service.correction;

import org.titiplex.desktop.domain.correction.Correction;
import org.titiplex.desktop.domain.correction.CorrectionDecision;
import org.titiplex.desktop.persistence.repository.CorrectionRepository;
import org.titiplex.desktop.persistence.repository.ExampleRepository;
import org.titiplex.desktop.persistence.repository.RuleRepository;

import java.util.List;

public final class CorrectionService {
    private final CorrectionRepository correctionRepository;
    private final ExampleRepository exampleRepository;
    private final RuleRepository ruleRepository;

    public CorrectionService(
            CorrectionRepository correctionRepository,
            ExampleRepository exampleRepository,
            RuleRepository ruleRepository
    ) {
        this.correctionRepository = correctionRepository;
        this.exampleRepository = exampleRepository;
        this.ruleRepository = ruleRepository;
    }

    public List<Correction> listAll() {
        return correctionRepository.findAll();
    }

    public List<Correction> listForExample(Long exampleId) {
        return correctionRepository.findByExampleId(exampleId);
    }

    public Correction save(Correction correction) {
        exampleRepository.findById(correction.exampleId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown example id: " + correction.exampleId()));

        if (correction.ruleId() != null) {
            ruleRepository.findById(correction.ruleId())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown rule id: " + correction.ruleId()));
        }

        return correctionRepository.save(correction);
    }

    public Correction decide(Correction correction, CorrectionDecision decision, String comment) {
        Correction updated = new Correction(
                correction.id(),
                correction.exampleId(),
                correction.ruleId(),
                correction.beforePayload(),
                correction.afterPayload(),
                decision,
                correction.origin(),
                comment,
                correction.createdAt()
        );
        return correctionRepository.save(updated);
    }
}
