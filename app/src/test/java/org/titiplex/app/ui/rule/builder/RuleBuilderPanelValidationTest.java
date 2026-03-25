package org.titiplex.app.ui.rule.builder;

import org.junit.jupiter.api.Test;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.ui.rule.builder.support.StringListTablePanel;

import javax.swing.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("SameParameterValue")
class RuleBuilderPanelValidationTest {

    @Test
    void generateConlluYamlRejectsEmptyGlobalConfig() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("global_config");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, panel::generateYaml);
        assertEquals("Global config cannot be empty.", exception.getMessage());
    }

    @Test
    void generateConlluYamlRejectsRuleUposWithoutUpos() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("rule_upos");
        getField(panel, "nameField", JTextField.class).setText("Rule upos");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, panel::generateYaml);
        assertEquals("set.upos is required for rule_upos.", exception.getMessage());
    }

    @Test
    void generateConlluYamlRejectsMixedLiteralAndStructuredGlossMatch() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("rule_upos");
        getField(panel, "nameField", JTextField.class).setText("Rule upos");
        getField(panel, "conlluSetUposField", JTextField.class).setText("VERB");
        getField(panel, "conlluMatchGlossLiteralField", JTextField.class).setText("FUT");
        getField(panel, "conlluMatchGlossRegexField", JTextField.class).setText("^DIR");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, panel::generateYaml);
        assertEquals("Choose either a literal gloss match or structured gloss criteria, not both.", exception.getMessage());
    }

    @Test
    void generateCorrectionYamlRejectsMixedTokenSequenceAndSelectors() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "idField", JTextField.class).setText("rule_1");
        getField(panel, "nameField", JTextField.class).setText("Rule 1");
        getField(panel, "matchTokensIswordPanel", StringListTablePanel.class).setValues(java.util.List.of("ha'"));


        Object tokenSequencePanel = getDeclaredField(panel, "matchTokenSequencesPanel");
        Field sequencesField = tokenSequencePanel.getClass().getDeclaredField("model");
        sequencesField.setAccessible(true);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) sequencesField.get(tokenSequencePanel);
        model.addRow(new Object[]{"ha', in"});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, panel::generateYaml);
        assertEquals("Use either token selector fields or token sequences, not both.", exception.getMessage());
    }

    private static Object getDeclaredField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}