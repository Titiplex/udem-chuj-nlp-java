package org.titiplex.desktop.bootstrap;

import java.nio.file.Path;

public final class AppPaths {
    private AppPaths() {
    }

    public static Path defaultAppHome() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "ChujNlpStudio");
        }
        return Path.of(System.getProperty("user.home"), ".chuj-nlp-studio");
    }
}
