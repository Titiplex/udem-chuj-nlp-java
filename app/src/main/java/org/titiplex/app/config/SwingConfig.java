package org.titiplex.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.titiplex.app.service.RuleService;
import org.titiplex.app.ui.frame.MainFrame;

@Configuration
public class SwingConfig {

    private final RuleService ruleService;

    public SwingConfig(RuleService ruleService) {
        this.ruleService = ruleService;
    }
    @Bean
    public MainFrame mainFrame() {
        return new MainFrame(ruleService);
    }
}
