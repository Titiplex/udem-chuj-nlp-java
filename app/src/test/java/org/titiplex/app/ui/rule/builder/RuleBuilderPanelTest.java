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

@SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "unchecked"})
class RuleBuilderPanelTest {

    @Test
    void generateCorrectionYamlKeepsAllGlossMatchCriteria() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "idField", JTextField.class).setText("rule_1");
        getField(panel, "nameField", JTextField.class).setText("Rule 1");

        getField(panel, "matchGlossPanel", StringListTablePanel.class).setValues(List.of("FUT", "PROSP"));
        getField(panel, "matchGlossStartsWithPanel", StringListTablePanel.class).setValues(List.of("DIR"));
        getField(panel, "matchGlossInLexiconField", JTextField.class).setText("verb_roots");

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
    void generateConlluGlobalConfigDoesNotInjectDefaultExtractor() throws Exception {
        RuleBuilderPanel panel = new RuleBuilderPanel();

        getField(panel, "kindBox", JComboBox.class).setSelectedItem(RuleKind.CONLLU);
        getField(panel, "conlluModeBox", JComboBox.class).setSelectedItem("global_config");

        String yamlText = panel.generateYaml();
        Map<String, Object> root = new Yaml().load(yamlText);

        assertFalse(root.containsKey("extractors"));
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}