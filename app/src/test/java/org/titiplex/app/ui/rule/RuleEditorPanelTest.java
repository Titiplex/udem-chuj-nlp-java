package org.titiplex.app.ui.rule;

import org.junit.jupiter.api.Test;
import org.titiplex.app.persistence.entity.Rule;
import org.titiplex.app.persistence.entity.RuleKind;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "unchecked"})
class RuleEditorPanelTest {

    @Test
    void toRuleSynchronizesMetadataIntoYaml() throws Exception {
        RuleEditorPanel panel = new RuleEditorPanel();

        getField(panel, "ruleIdField", JTextField.class).setText("edited_rule");
        getField(panel, "nameField", JTextField.class).setText("Edited Rule");
        getField(panel, "descriptionArea", JTextArea.class).setText("Edited description");
        getField(panel, "yamlArea", JTextArea.class).setText("""
                def:
                  pos: [VERB]
                rules:
                  - id: old_rule
                    name: Old Rule
                    description: old description
                    rewrite:
                      before: [a]
                      after: [b]
                """);

        Rule rule = panel.toRule();

        Map<String, Object> root = new Yaml().load(rule.getYamlBody());
        List<Map<String, Object>> rules = (List<Map<String, Object>>) root.get("rules");
        Map<String, Object> first = rules.get(0);

        assertEquals("edited_rule", first.get("id"));
        assertEquals("Edited Rule", first.get("name"));
        assertEquals("Edited description", first.get("description"));
    }

    @Test
    void toRuleRejectsYamlWithMultipleRules() throws Exception {
        RuleEditorPanel panel = new RuleEditorPanel();

        getField(panel, "ruleIdField", JTextField.class).setText("edited_rule");
        getField(panel, "nameField", JTextField.class).setText("Edited Rule");
        getField(panel, "yamlArea", JTextArea.class).setText("""
                rules:
                  - id: a
                    name: A
                  - id: b
                    name: B
                """);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, panel::toRule);
        assertTrue(ex.getMessage().contains("exactly one rule"));
    }

    @Test
    void toRuleRejectsCorrectionYamlWithoutRules() throws Exception {
        RuleEditorPanel panel = new RuleEditorPanel();

        getField(panel, "ruleIdField", JTextField.class).setText("edited_rule");
        getField(panel, "nameField", JTextField.class).setText("Edited Rule");
        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CORRECTION);
        getField(panel, "yamlArea", JTextArea.class).setText("def:\n  pos: [VERB]\n");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, panel::toRule);
        assertTrue(ex.getMessage().contains("must contain exactly one rule"));
    }

    @Test
    void toRuleAllowsConlluGlobalConfigWithoutRules() throws Exception {
        RuleEditorPanel panel = new RuleEditorPanel();

        getField(panel, "ruleIdField", JTextField.class).setText("globals");
        getField(panel, "nameField", JTextField.class).setText("Globals");
        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "yamlArea", JTextArea.class).setText("""
                def:
                  pos: [VERB]
                lexicons:
                  verbs: verbs.txt
                """);

        Rule rule = panel.toRule();

        Map<String, Object> root = new Yaml().load(rule.getYamlBody());
        assertEquals(Map.of("pos", List.of("VERB")), root.get("def"));
        assertFalse(root.containsKey("rules"));
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}