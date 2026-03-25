package org.titiplex.app;

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.NoArgsConstructor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.titiplex.app.ui.frame.MainFrame;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
@NoArgsConstructor
public class Main {
    public static void main(String[] args) {
        FlatDarkLaf.setup();

        ConfigurableApplicationContext context = new SpringApplicationBuilder(Main.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = context.getBean(MainFrame.class);
            AtomicBoolean closing = new AtomicBoolean(false);

            Runnable shutdown = () -> {
                if (!closing.compareAndSet(false, true)) {
                    return;
                }
                try {
                    frame.dispose();
                } finally {
                    context.close();
                }
            };

            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.setQuitAction(shutdown);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    shutdown.run();
                }
            });
            frame.setVisible(true);
        });
    }
}