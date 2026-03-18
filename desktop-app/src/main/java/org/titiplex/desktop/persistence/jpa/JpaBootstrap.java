package org.titiplex.desktop.persistence.jpa;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;
import org.titiplex.desktop.bootstrap.AppConfig;

import java.util.HashMap;
import java.util.Map;

public final class JpaBootstrap {
    private final AppConfig config;

    public JpaBootstrap(AppConfig config) {
        this.config = config;
    }

    public void migrate() {
        config.databasePath().getParent().toFile().mkdirs();

        Flyway.configure()
                .dataSource(jdbcUrl(), "sa", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }

    public EntityManagerFactory buildEntityManagerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", jdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        return Persistence.createEntityManagerFactory(config.persistenceUnit(), properties);
    }

    private String jdbcUrl() {
        return "jdbc:h2:file:" + config.databasePath().toAbsolutePath() + ";AUTO_SERVER=TRUE";
    }
}
