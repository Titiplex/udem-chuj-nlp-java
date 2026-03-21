package org.titiplex.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.titiplex.app.ui.frame.MainFrame;

@Configuration
public class SwingConfig {
    @Bean
    public MainFrame mainFrame() {
        return new MainFrame();
    }
}
