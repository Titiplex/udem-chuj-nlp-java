package org.titiplex.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.titiplex.app.service.CorrectedEntryService;
import org.titiplex.app.service.RawEntryService;
import org.titiplex.app.service.RuleService;
import org.titiplex.app.ui.frame.MainFrame;

@Configuration
public class SwingConfig {

    private final RuleService ruleService;
    private final CorrectedEntryService correctedEntryService;
    private final RawEntryService rawEntryService;

    public SwingConfig(
            RuleService ruleService,
            CorrectedEntryService correctedEntryService,
            RawEntryService rawEntryService
    ) {
        this.ruleService = ruleService;
        this.correctedEntryService = correctedEntryService;
        this.rawEntryService = rawEntryService;
    }
    @Bean
    public MainFrame mainFrame() {
        return new MainFrame(ruleService, correctedEntryService, rawEntryService);
    }
}
