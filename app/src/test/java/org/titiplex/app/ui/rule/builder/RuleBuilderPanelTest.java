package org.titiplex.app.ui.rule.builder;

import org.junit.jupiter.api.Test;
import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.ui.rule.builder.support.StringListTablePanel;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "unchecked", "SameParameterValue"})
class RuleBuilderPanelTest {

    @Test
    void generateCorrectionYamlKeepsAllGlossMatchCriteria() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "idField", JTextField.class).setText("rule_1");
        getField(panel, "nameField", JTextField.class).setText("Rule 1");

        getField(panel, "matchGlossPanel", StringListTablePanel.class).setValues(List.of("FUT", "PROSP"));
        getField(panel, "matchGlossStartsWithPanel", StringListTablePanel.class).setValues(List.of("DIR"));
        getField(panel, "matchGlossInLexiconField", JTextField.class).setText("verb_roots");

        getField(panel, "surfaceBeforeField", JTextField.class).setText("a");
        getField(panel, "surfaceAfterField", JTextField.class).setText("b");

        String yamlText = panel.generateYaml();

        Map<String, Object> root = new Yaml().load(yamlText);
        List<Map<String, Object>> rules = (List<Map<String, Object>>) root.get("rules");
        Map<String, Object> rule = rules.get(0);
        Map<String, Object> rewrite = (Map<String, Object>) rule.get("rewrite");
        Map<String, Object> match = (Map<String, Object>) rewrite.get("match");
        Map<String, Object> gloss = (Map<String, Object>) match.get("gloss");

        assertEquals(List.of("FUT", "PROSP"), gloss.get("any"));
        assertEquals(List.of("DIR"), gloss.get("starts_with"));
        assertEquals("verb_roots", gloss.get("in_lexicon"));
    }

    @Test
    void generateCorrectionYamlPlacesTargetsAndBetweenWhereCoreParserExpectsThem() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "idField", JTextField.class).setText("rule_1");
        getField(panel, "nameField", JTextField.class).setText("Rule 1");
        getField(panel, "surfaceBeforeField", JTextField.class).setText("a");
        getField(panel, "surfaceAfterField", JTextField.class).setText("b");
        getField(panel, "useBetweenLengthBox", JCheckBox.class).setSelected(true);
        getField(panel, "betweenLengthSpinner", JSpinner.class).setValue(2);
        getField(panel, "targetsField", JTextField.class).setText("i");

        String yamlText = panel.generateYaml();

        Map<String, Object> root = new Yaml().load(yamlText);
        List<Map<String, Object>> rules = (List<Map<String, Object>>) root.get("rules");
        Map<String, Object> rule = rules.get(0);
        Map<String, Object> rewrite = (Map<String, Object>) rule.get("rewrite");
        Map<String, Object> between = (Map<String, Object>) rewrite.get("between");

        assertEquals("i", rule.get("targets"));
        assertEquals(2, between.get("length"));

        Map<String, Object> match = (Map<String, Object>) rewrite.get("match");
        assertFalse(match.containsKey("between"));
        assertFalse(match.containsKey("targets"));
    }

    @Test
    void generateCorrectionYamlRejectsTokenSequencesCombinedWithTokenSelectors() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "idField", JTextField.class).setText("rule_1");
        getField(panel, "nameField", JTextField.class).setText("Rule 1");
        getField(panel, "surfaceBeforeField", JTextField.class).setText("a");
        getField(panel, "surfaceAfterField", JTextField.class).setText("b");

        getField(panel, "matchGlossPanel", StringListTablePanel.class).setValues(List.of("FUT"));
        getField(panel, "matchTokensIswordPanel", StringListTablePanel.class).setValues(List.of("ha"));
        getField(panel, "matchTokenSequencesPanel", org.titiplex.app.ui.rule.builder.support.TokenSequenceTablePanel.class)
                .getClass(); // ensure field exists

        Field seqField = panel.getClass().getDeclaredField("matchTokenSequencesPanel");
        seqField.setAccessible(true);
        Object seqPanel = seqField.get(panel);
        var setSeq = seqPanel.getClass().getDeclaredMethod("getSequences");
        setSeq.setAccessible(true);
        // cannot set directly, so use reflection on model via helper below
        setFirstSequence(seqPanel, "ha,tik");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, panel::generateYaml);
        assertTrue(ex.getMessage().contains("either token sequences or token selectors"));
    }

    @Test
    void generateConlluGlobalConfigDoesNotInjectDefaultExtractor() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("global_config");

        getField(panel, "defPosPanel", StringListTablePanel.class).setValues(List.of("VERB"));

        String yamlText = panel.generateYaml();
        Map<String, Object> root = new Yaml().load(yamlText);

        assertFalse(root.containsKey("extractors"));
    }

    @Test
    void generateConlluYamlRejectsEmptyGlobalConfig() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("global_config");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, panel::generateYaml);
        assertTrue(ex.getMessage().contains("Global config"));
    }

    @Test
    void generateConlluYamlRejectsLiteralAndStructuredGlossTogether() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("rule_upos");
        getField(panel, "nameField", JTextField.class).setText("upos rule");
        getField(panel, "conlluSetUposField", JTextField.class).setText("VERB");
        getField(panel, "conlluMatchGlossLiteralField", JTextField.class).setText("A1");
        getField(panel, "conlluMatchGlossRegexField", JTextField.class).setText("^A");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, panel::generateYaml);
        assertTrue(ex.getMessage().contains("literal gloss match"));
    }

    @Test
    void refreshFieldVisibilityShowsOnlyRelevantCorrectionSections() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "correctionActionBox", JComboBox.class).setSelectedItem("regex_sub");

        JPanel regexSection = getField(panel, "correctionRegexSection", JPanel.class);
        JPanel splitSection = getField(panel, "correctionSplitSuffixSection", JPanel.class);
        JPanel mergeSection = getField(panel, "correctionMergeSection", JPanel.class);

        assertTrue(regexSection.isVisible());
        assertFalse(splitSection.isVisible());
        assertFalse(mergeSection.isVisible());
    }

    @Test
    void refreshFieldVisibilityShowsOnlyRelevantConlluSections() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("global_config");

        JPanel matchSection = getField(panel, "conlluMatchSection", JPanel.class);
        JPanel globalsSection = getField(panel, "conlluGlobalsDefinitionsSection", JPanel.class);
        JPanel uposSection = getField(panel, "conlluSetUposSection", JPanel.class);

        assertFalse(matchSection.isVisible());
        assertTrue(globalsSection.isVisible());
        assertFalse(uposSection.isVisible());
    }

    private static void setFirstSequence(Object tokenSequencePanel, String value) throws Exception {
        Field modelField = tokenSequencePanel.getClass().getDeclaredField("model");
        modelField.setAccessible(true);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) modelField.get(tokenSequencePanel);
        model.addRow(new Object[]{value});
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}