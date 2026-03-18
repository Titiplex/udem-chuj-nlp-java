package org.titiplex.desktop.bootstrap;

import java.nio.file.Path;

public record AppConfig(
        String appName,
        String persistenceUnit,
        Path appHome,
        Path databasePath,
        boolean darkTheme
) {
    public static AppConfig defaultConfig() {
        Path appHome = AppPaths.defaultAppHome();
        return new AppConfig(
                "Chuj NLP Studio",
                "chujDesktopPU",
                appHome,
                appHome.resolve("db").resolve("chuj-studio"),
                true
        );
    }
}
