package org.titiplex.app;

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.NoArgsConstructor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.titiplex.app.ui.frame.MainFrame;

import javax.swing.*;

@SpringBootApplication
@NoArgsConstructor
public class Main {
    public static void main(String[] args) {
        // flat laf should be initialized before spring application context
        FlatDarkLaf.setup();

        ConfigurableApplicationContext context = new SpringApplicationBuilder(Main.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = context.getBean(MainFrame.class);
            frame.setVisible(true);
        });
    }
}
