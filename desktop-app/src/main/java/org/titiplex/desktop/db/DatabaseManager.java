package org.titiplex.desktop.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private final String url;

    public DatabaseManager(Path dbPath) {
        this.url = "jdbc:h2:file:" + dbPath.toAbsolutePath() + ";AUTO_SERVER=TRUE";
    }

    public void init() {
        try (Connection connection = open(); Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    create table IF not exists rules (
                      id identity primary key,
                      rule_id varchar(200) not null,
                      name varchar(200) not null,
                      enabled BOOLEAN not null,
                      yaml_body CLOB not null,
                      source_file varchar(255),
                      updated_at timestamp not null default current_timestamp
                    )
                    """);
            stmt.execute("create index IF not exists idx_rules_rule_id on rules(rule_id)");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }
}
