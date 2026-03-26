package org.titiplex.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.titiplex.app.persistence.entity.CorrectedEntry;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.persistence.repository.RuleRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RulesetFingerprintService {
    public static final String EMPTY_CORRECTION_RULESET = "NO_CORRECTION_RULES";

    private final RuleRepository ruleRepository;

    public RulesetFingerprintService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public String currentCorrectionRulesetFingerprint() {
        List<Rule> rules = ruleRepository.findAllByEnabledTrueAndKindOrderByStableIdAsc(RuleKind.CORRECTION);
        if (rules.isEmpty()) {
            return EMPTY_CORRECTION_RULESET;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Rule rule : rules) {
                update(digest, rule.getStableId());
                update(digest, String.valueOf(rule.getVersionNo()));
                update(digest, rule.getYamlBody());
                update(digest, String.valueOf(rule.isEnabled()));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to compute correction ruleset fingerprint", exception);
        }
    }

    public boolean isCorrectionRulesetOutdated(CorrectedEntry entry) {
        if (entry == null || !Boolean.TRUE.equals(entry.getIsCorrect())) {
            return false;
        }
        String approvedFingerprint = entry.getApprovedRulesFingerprint();
        if (approvedFingerprint == null || approvedFingerprint.isBlank()) {
            return true;
        }
        return !approvedFingerprint.equals(currentCorrectionRulesetFingerprint());
    }

    private void update(MessageDigest digest, String value) {
        String normalized = value == null ? "" : value;
        digest.update(normalized.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }
}
