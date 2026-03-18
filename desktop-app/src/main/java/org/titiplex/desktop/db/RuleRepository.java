package org.titiplex.desktop.db;

import org.titiplex.desktop.model.RuleRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class RuleRepository {
    private final DatabaseManager databaseManager;

    public RuleRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<RuleRecord> findAll() {
        String sql = "select id, rule_id, name, enabled, yaml_body, source_file, updated_at from rules order by rule_id";
        try (Connection connection = databaseManager.open();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RuleRecord> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list rules", e);
        }
    }

    public void replaceAll(List<RuleRecord> rules) {
        try (Connection connection = databaseManager.open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement("delete from rules")) {
                delete.executeUpdate();
            }
            String insertSql = "insert into rules(rule_id, name, enabled, yaml_body, source_file, updated_at) values (?, ?, ?, ?, ?, current_timestamp)";
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                for (RuleRecord rule : rules) {
                    insert.setString(1, rule.ruleId());
                    insert.setString(2, rule.name());
                    insert.setBoolean(3, rule.enabled());
                    insert.setString(4, rule.yamlBody());
                    insert.setString(5, rule.sourceFile());
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to replace rules", e);
        }
    }

    public void save(RuleRecord rule) {
        if (rule.id() == null) {
            insert(rule);
            return;
        }
        String sql = "update rules set rule_id=?, name=?, enabled=?, yaml_body=?, source_file=?, updated_at=current_timestamp where id=?";
        try (Connection connection = databaseManager.open();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rule.ruleId());
            ps.setString(2, rule.name());
            ps.setBoolean(3, rule.enabled());
            ps.setString(4, rule.yamlBody());
            ps.setString(5, rule.sourceFile());
            ps.setLong(6, rule.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update rule", e);
        }
    }

    private void insert(RuleRecord rule) {
        String sql = "insert into rules(rule_id, name, enabled, yaml_body, source_file, updated_at) values (?, ?, ?, ?, ?, current_timestamp)";
        try (Connection connection = databaseManager.open();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rule.ruleId());
            ps.setString(2, rule.name());
            ps.setBoolean(3, rule.enabled());
            ps.setString(4, rule.yamlBody());
            ps.setString(5, rule.sourceFile());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert rule", e);
        }
    }

    private static RuleRecord map(ResultSet rs) throws SQLException {
        return new RuleRecord(
                rs.getLong("id"),
                rs.getString("rule_id"),
                rs.getString("name"),
                rs.getBoolean("enabled"),
                rs.getString("yaml_body"),
                rs.getString("source_file"),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
