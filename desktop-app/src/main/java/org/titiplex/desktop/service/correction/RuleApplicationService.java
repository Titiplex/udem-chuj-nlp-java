package org.titiplex.desktop.service.correction;

import org.titiplex.desktop.domain.validation.ValidationRun;
import org.titiplex.desktop.service.rule.RuleValidationService;

public final class RuleApplicationService {
    private final RuleValidationService validationService;

    public RuleApplicationService(RuleValidationService validationService) {
        this.validationService = validationService;
    }

    public ValidationRun dryRunRules() {
        return validationService.validateAll();
    }
}
