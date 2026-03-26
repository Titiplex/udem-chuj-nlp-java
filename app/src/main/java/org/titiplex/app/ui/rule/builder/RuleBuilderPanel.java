package org.titiplex.app.ui.rule.builder;

import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.ui.common.Dialogs;
import org.titiplex.app.ui.rule.builder.support.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuleBuilderPanel extends JPanel {
    private final JComboBox<RuleKind> kindBox = new JComboBox<>(RuleKind.values());
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(3, 60);

    private final JLabel errorLabel = new JLabel(" ");
    private final JTextArea previewArea = new JTextArea(20, 80);

    private final CardLayout rootCardLayout = new CardLayout();
    private final JPanel rootCards = new JPanel(rootCardLayout);

    // correction
    private final JComboBox<String> correctionActionBox = new JComboBox<>(new String[]{
            "rewrite_before_after",
            "rewrite_gloss_only",
            "regex_sub",
            "split_suffix",
            "split_suffix_with_final_gloss",
            "delete_chars",
            "delete_part",
            "insert_segment",
            "merge_tokens"
    });
    private final JComboBox<String> regexScopeBox = new JComboBox<>(new String[]{"chuj", "gloss"});
    private final JCheckBox regexIgnoreCaseBox = new JCheckBox("Ignore case");
    private final JTextField surfaceBeforeField = new JTextField();
    private final JTextField surfaceAfterField = new JTextField();
    private final JTextField glossBeforeField = new JTextField();
    private final JTextField glossAfterField = new JTextField();
    private final JTextField regexPatternField = new JTextField();
    private final JTextField regexReplacementField = new JTextField();
    private final JTextField splitSuffixesField = new JTextField();
    private final JComboBox<String> glossPlacementBox = new JComboBox<>(new String[]{"right", "left"});
    private final StringListTablePanel splitGlossLastStartsWithPanel = new StringListTablePanel("gloss_last_match.starts_with", "Value");
    private final JTextField deleteValuesField = new JTextField();
    private final JTextField insertSegmentField = new JTextField();
    private final JSpinner insertTokenSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JSpinner insertPositionSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

    private final StringListTablePanel matchGlossPanel = new StringListTablePanel("match.gloss", "Gloss");
    private final StringListTablePanel matchGlossStartsWithPanel = new StringListTablePanel("match.gloss.starts_with", "Prefix");
    private final JTextField matchGlossInLexiconField = new JTextField();

    private final StringListTablePanel matchTokensIswordPanel = new StringListTablePanel("tokens.isword", "Token");
    private final StringListTablePanel matchTokensAnyPanel = new StringListTablePanel("tokens.any", "Token");
    private final StringListTablePanel matchTokensStartsWithPanel = new StringListTablePanel("tokens.startswith", "Prefix");
    private final StringListTablePanel matchTokensEndsWithPanel = new StringListTablePanel("tokens.endswith", "Suffix");
    private final StringListTablePanel matchTokensHasSegmentPanel = new StringListTablePanel("tokens.has_segment", "Segment");
    private final JCheckBox matchStartsWithVowelBox = new JCheckBox("tokens.startswith_vowel");
    private final TokenSequenceTablePanel matchTokenSequencesPanel = new TokenSequenceTablePanel("match token sequences");

    private final JTextField surfaceSideField = new JTextField();
    private final JTextField surfaceRootInLexiconField = new JTextField();
    private final JCheckBox surfaceRootStartsWithVowelBox = new JCheckBox("surface.root_startswith_vowel");
    private final JCheckBox useBetweenLengthBox = new JCheckBox("Use between.length");
    private final JSpinner betweenLengthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JTextField targetsField = new JTextField();

    private final TokenSequenceTablePanel mergeSequencesPanel = new TokenSequenceTablePanel("merge.match.tokens");

    // conllu rule
    private final JComboBox<String> conlluModeBox = new JComboBox<>(new String[]{
            "rule_upos",
            "rule_feats",
            "rule_extract",
            "rule_full",
            "global_config"
    });
    private final JComboBox<String> conlluScopeBox = new JComboBox<>(new String[]{"token", "morpheme"});
    private final JSpinner conlluPrioritySpinner = new JSpinner(new SpinnerNumberModel(0, -100, 1000, 1));
    private final JTextField conlluMatchRegexField = new JTextField();
    private final JTextField conlluMatchGlossLiteralField = new JTextField();
    private final JTextField conlluMatchGlossRegexField = new JTextField();
    private final JTextField conlluMatchGlossInLexiconField = new JTextField();

    private final StringListTablePanel conlluMatchInListPanel = new StringListTablePanel("match.in_list", "Value / file");
    private final StringListTablePanel conlluMatchRequirePanel = new StringListTablePanel("match.require", "Value");
    private final StringListTablePanel conlluMatchForbidPanel = new StringListTablePanel("match.forbid", "Value");
    private final StringListTablePanel conlluMatchGlossInListPanel = new StringListTablePanel("match.gloss.in_list", "Value / file");
    private final StringListTablePanel conlluMatchGlossRequirePanel = new StringListTablePanel("match.gloss.require", "Value");
    private final StringListTablePanel conlluMatchGlossForbidPanel = new StringListTablePanel("match.gloss.forbid", "Value");
    private final ExtractSpecTablePanel conlluMatchExtractPanel = new ExtractSpecTablePanel("match.extract");
    private final JTextField conlluSetUposField = new JTextField();
    private final KeyValueTablePanel conlluFeatsPanel = new KeyValueTablePanel("set.feats", "Feat", "Value");
    private final KeyValueTablePanel conlluFeatsTemplatePanel = new KeyValueTablePanel("set.feats_template", "Feat", "Template");
    private final ExtractSpecTablePanel conlluSetExtractPanel = new ExtractSpecTablePanel("set.extract");

    // conllu globals
    private final JTextField lexiconNameField = new JTextField();
    private final JTextField lexiconFileField = new JTextField();
    private final StringListTablePanel defPosPanel = new StringListTablePanel("def.pos", "POS");
    private final StringListTablePanel defFeatsPanel = new StringListTablePanel("def.feats", "Feature");
    private final KeyValueTablePanel glossMapPosPanel = new KeyValueTablePanel("gloss_map.pos", "Gloss", "UPOS");
    private final GlossFeatMapTablePanel glossMapFeatsPanel = new GlossFeatMapTablePanel("gloss_map.feats");
    private final JTextField extractorNameField = new JTextField();
    private final KeyValueTablePanel extractorSeriesPanel = new KeyValueTablePanel("extractor series", "Series", "Role");
    private final StringListTablePanel extractorPersonsPanel = new StringListTablePanel("extractor persons", "Person");
    private final JTextField extractorNumberSuffixField = new JTextField();
    private final RoutingRuleTablePanel routingRulePanel = new RoutingRuleTablePanel("routing");
    private final StringListTablePanel extractorsFilePanel = new StringListTablePanel("extractors_file", "Path");
    private final StringListTablePanel rulesFilePanel = new StringListTablePanel("rules_file", "Path");

    // correction sections
    private JPanel correctionMatchGlossSection;
    private JPanel correctionMatchTokensSection;
    private JPanel correctionSurfaceSection;
    private JPanel correctionBetweenTargetsSection;
    private JPanel correctionRewriteBeforeAfterSection;
    private JPanel correctionRewriteGlossOnlySection;
    private JPanel correctionRegexSection;
    private JPanel correctionSplitSuffixSection;
    private JPanel correctionSplitSuffixFinalGlossSection;
    private JPanel correctionDeleteSection;
    private JPanel correctionInsertSection;
    private JPanel correctionMergeSection;

    // conllu sections
    private JPanel conlluRuleMetaSection;
    private JPanel conlluMatchSection;
    private JPanel conlluSetUposSection;
    private JPanel conlluSetFeatsSection;
    private JPanel conlluSetExtractSection;
    private JPanel conlluGlobalsLexiconSection;
    private JPanel conlluGlobalsDefinitionsSection;
    private JPanel conlluGlobalsGlossMapSection;
    private JPanel conlluGlobalsExtractorSection;
    private JPanel conlluGlobalsFilesSection;

    private Runnable loadIntoEditorAction;
    private final Yaml yaml;

    public RuleBuilderPanel() {
        setLayout(new BorderLayout());

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        yaml = new Yaml(options);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        errorLabel.setForeground(new Color(190, 70, 70));

        rootCards.add(buildCorrectionCard(), RuleKind.CORRECTION.name());
        rootCards.add(buildConlluCard(), RuleKind.CONLLU.name());

        JPanel formRoot = new JPanel();
        formRoot.setLayout(new BoxLayout(formRoot, BoxLayout.Y_AXIS));
        formRoot.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel metaPanel = buildMetaPanel();
        metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootCards.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton previewButton = new JButton("Generate preview");
        previewButton.addActionListener(e -> safelyGeneratePreview(true));
        JButton loadButton = new JButton("Load into YAML editor");
        loadButton.addActionListener(e -> safelyLoadIntoEditor());

        actions.add(previewButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(loadButton);

        formRoot.add(metaPanel);
        formRoot.add(Box.createVerticalStrut(8));
        formRoot.add(rootCards);
        formRoot.add(Box.createVerticalStrut(8));
        formRoot.add(actions);
        formRoot.add(Box.createVerticalStrut(6));
        formRoot.add(errorLabel);

        JScrollPane formScroll = new JScrollPane(formRoot);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setBorder(BorderFactory.createTitledBorder("YAML preview"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formScroll, previewScroll);
        splitPane.setResizeWeight(0.72);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        kindBox.addActionListener(e -> refreshFieldVisibility());
        correctionActionBox.addActionListener(e -> refreshFieldVisibility());
        conlluModeBox.addActionListener(e -> refreshFieldVisibility());

        refreshFieldVisibility();
    }

    public void setLoadIntoEditorAction(Runnable action) {
        this.loadIntoEditorAction = action;
    }

    public RuleKind getSelectedKind() {
        return (RuleKind) kindBox.getSelectedItem();
    }

    public String getPreviewYaml() {
        return previewArea.getText();
    }

    private JPanel buildMetaPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Guided rule builder"));

        GridBagConstraints c = baseConstraints();
        addRow(panel, c, 0, "Rule kind", kindBox);
        addRow(panel, c, 1, "Rule id", idField);
        addRow(panel, c, 2, "Name", nameField);

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(220, 90));
        addRow(panel, c, 3, "Description", descriptionScroll);

        return panel;
    }

    private JPanel buildCorrectionCard() {
        JPanel panel = verticalCard("Correction rule");

        correctionMatchGlossSection = buildSection(
                "Match · gloss",
                matchGlossPanel,
                labeledLine("match.gloss.in_lexicon", matchGlossInLexiconField),
                matchGlossStartsWithPanel
        );

        correctionMatchTokensSection = buildSection(
                "Match · tokens",
                matchTokensIswordPanel,
                matchTokensAnyPanel,
                matchTokensStartsWithPanel,
                matchTokensEndsWithPanel,
                matchTokensHasSegmentPanel,
                matchStartsWithVowelBox,
                matchTokenSequencesPanel
        );

        correctionSurfaceSection = buildSection(
                "Match · surface/root",
                labeledLine("surface.side", surfaceSideField),
                labeledLine("surface.root_in_lexicon", surfaceRootInLexiconField),
                surfaceRootStartsWithVowelBox
        );

        correctionBetweenTargetsSection = buildSection(
                "Match · relation / targets",
                useBetweenLengthBox,
                labeledLine("between.length", betweenLengthSpinner),
                labeledLine("targets", targetsField)
        );

        correctionRewriteBeforeAfterSection = buildSection(
                "Action · rewrite before/after",
                labeledLine("surface before (csv)", surfaceBeforeField),
                labeledLine("surface after (csv)", surfaceAfterField),
                labeledLine("gloss before (csv)", glossBeforeField),
                labeledLine("gloss after (csv)", glossAfterField)
        );

        correctionRewriteGlossOnlySection = buildSection(
                "Action · gloss only rewrite",
                labeledLine("gloss before (csv)", glossBeforeField),
                labeledLine("gloss after (csv)", glossAfterField)
        );

        correctionRegexSection = buildSection(
                "Action · regex substitution",
                labeledLine("regex scope", regexScopeBox),
                regexIgnoreCaseBox,
                labeledLine("regex pattern", regexPatternField),
                labeledLine("regex replacement", regexReplacementField)
        );

        correctionSplitSuffixSection = buildSection(
                "Action · split suffix",
                labeledLine("split suffixes (csv)", splitSuffixesField),
                labeledLine("gloss placement", glossPlacementBox)
        );

        correctionSplitSuffixFinalGlossSection = buildSection(
                "Action · split suffix with final gloss",
                labeledLine("split suffixes (csv)", splitSuffixesField),
                splitGlossLastStartsWithPanel
        );

        correctionDeleteSection = buildSection(
                "Action · delete",
                labeledLine("delete values (csv)", deleteValuesField)
        );

        correctionInsertSection = buildSection(
                "Action · insert segment",
                labeledLine("insert segment", insertSegmentField),
                labeledLine("insert token", insertTokenSpinner),
                labeledLine("insert position", insertPositionSpinner)
        );

        correctionMergeSection = buildSection(
                "Action · merge tokens",
                mergeSequencesPanel
        );

        panel.add(labeledLine("Action", correctionActionBox));
        panel.add(correctionMatchGlossSection);
        panel.add(correctionMatchTokensSection);
        panel.add(correctionSurfaceSection);
        panel.add(correctionBetweenTargetsSection);
        panel.add(correctionRewriteBeforeAfterSection);
        panel.add(correctionRewriteGlossOnlySection);
        panel.add(correctionRegexSection);
        panel.add(correctionSplitSuffixSection);
        panel.add(correctionSplitSuffixFinalGlossSection);
        panel.add(correctionDeleteSection);
        panel.add(correctionInsertSection);
        panel.add(correctionMergeSection);
        return panel;
    }

    private JPanel buildConlluCard() {
        JPanel panel = verticalCard("CoNLL-U");

        conlluRuleMetaSection = buildSection(
                "Rule · metadata",
                labeledLine("Mode", conlluModeBox),
                labeledLine("Scope", conlluScopeBox),
                labeledLine("Priority", conlluPrioritySpinner)
        );

        conlluMatchSection = buildSection(
                "Rule · match",
                labeledLine("match.regex", conlluMatchRegexField),
                labeledLine("match.gloss literal", conlluMatchGlossLiteralField),
                labeledLine("match.gloss.regex", conlluMatchGlossRegexField),
                labeledLine("match.gloss.in_lexicon", conlluMatchGlossInLexiconField),
                conlluMatchInListPanel,
                conlluMatchRequirePanel,
                conlluMatchForbidPanel,
                conlluMatchGlossInListPanel,
                conlluMatchGlossRequirePanel,
                conlluMatchGlossForbidPanel,
                conlluMatchExtractPanel
        );

        conlluSetUposSection = buildSection(
                "Rule · set.upos",
                labeledLine("set.upos", conlluSetUposField)
        );

        conlluSetFeatsSection = buildSection(
                "Rule · set.feats",
                conlluFeatsPanel,
                conlluFeatsTemplatePanel
        );

        conlluSetExtractSection = buildSection(
                "Rule · set.extract",
                conlluSetExtractPanel
        );

        JPanel lexiconFilePanel = new JPanel(new BorderLayout(6, 0));
        lexiconFilePanel.add(lexiconFileField, BorderLayout.CENTER);
        JButton browseLexiconButton = new JButton("Browse...");
        browseLexiconButton.addActionListener(e -> chooseFileInto(lexiconFileField));
        lexiconFilePanel.add(browseLexiconButton, BorderLayout.EAST);

        conlluGlobalsLexiconSection = buildSection(
                "Globals · lexicons",
                labeledLine("lexicon name", lexiconNameField),
                labeledLine("lexicon file", lexiconFilePanel)
        );

        conlluGlobalsDefinitionsSection = buildSection(
                "Globals · definitions",
                defPosPanel,
                defFeatsPanel
        );

        conlluGlobalsGlossMapSection = buildSection(
                "Globals · gloss map",
                glossMapPosPanel,
                glossMapFeatsPanel
        );

        conlluGlobalsExtractorSection = buildSection(
                "Globals · extractors",
                labeledLine("extractor name", extractorNameField),
                extractorSeriesPanel,
                extractorPersonsPanel,
                labeledLine("extractor number suffix", extractorNumberSuffixField),
                routingRulePanel
        );

        conlluGlobalsFilesSection = buildSection(
                "Globals · external files",
                extractorsFilePanel,
                rulesFilePanel
        );

        panel.add(conlluRuleMetaSection);
        panel.add(conlluMatchSection);
        panel.add(conlluSetUposSection);
        panel.add(conlluSetFeatsSection);
        panel.add(conlluSetExtractSection);
        panel.add(conlluGlobalsLexiconSection);
        panel.add(conlluGlobalsDefinitionsSection);
        panel.add(conlluGlobalsGlossMapSection);
        panel.add(conlluGlobalsExtractorSection);
        panel.add(conlluGlobalsFilesSection);

        return panel;
    }

    private JPanel verticalCard(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel buildSection(String title, Component... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        for (Component component : components) {
            if (component instanceof JComponent jc) {
                jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            panel.add(component);
        }
        return panel;
    }

    private JPanel labeledLine(String label, Component component) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(2, 0, 2, 0));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void refreshFieldVisibility() {
        RuleKind kind = getSelectedKind();
        rootCardLayout.show(rootCards, kind.name());

        String action = String.valueOf(correctionActionBox.getSelectedItem());
        boolean mergeAction = "merge_tokens".equals(action);

        correctionMatchGlossSection.setVisible(!mergeAction);
        correctionMatchTokensSection.setVisible(!mergeAction);
        correctionSurfaceSection.setVisible(!mergeAction);
        correctionBetweenTargetsSection.setVisible(!mergeAction);

        correctionRewriteBeforeAfterSection.setVisible("rewrite_before_after".equals(action));
        correctionRewriteGlossOnlySection.setVisible("rewrite_gloss_only".equals(action));
        correctionRegexSection.setVisible("regex_sub".equals(action));
        correctionSplitSuffixSection.setVisible("split_suffix".equals(action));
        correctionSplitSuffixFinalGlossSection.setVisible("split_suffix_with_final_gloss".equals(action));
        correctionDeleteSection.setVisible("delete_chars".equals(action) || "delete_part".equals(action));
        correctionInsertSection.setVisible("insert_segment".equals(action));
        correctionMergeSection.setVisible(mergeAction);

        String conlluMode = String.valueOf(conlluModeBox.getSelectedItem());
        boolean globalOnly = "global_config".equals(conlluMode);
        boolean withGlobals = "global_config".equals(conlluMode) || "rule_full".equals(conlluMode);

        conlluRuleMetaSection.setVisible(true);
        conlluMatchSection.setVisible(!globalOnly);
        conlluSetUposSection.setVisible("rule_upos".equals(conlluMode) || "rule_full".equals(conlluMode));
        conlluSetFeatsSection.setVisible("rule_feats".equals(conlluMode) || "rule_full".equals(conlluMode));
        conlluSetExtractSection.setVisible("rule_extract".equals(conlluMode) || "rule_full".equals(conlluMode));
        conlluGlobalsLexiconSection.setVisible(withGlobals);
        conlluGlobalsDefinitionsSection.setVisible(withGlobals);
        conlluGlobalsGlossMapSection.setVisible(withGlobals);
        conlluGlobalsExtractorSection.setVisible(withGlobals);
        conlluGlobalsFilesSection.setVisible(withGlobals);

        revalidate();
        repaint();
    }

    private void safelyGeneratePreview(boolean dialogOnError) {
        clearError();
        try {
            previewArea.setText(generateYaml());
            previewArea.setCaretPosition(0);
        } catch (Exception ex) {
            showError(ex, dialogOnError);
        }
    }

    private void safelyLoadIntoEditor() {
        clearError();
        try {
            previewArea.setText(generateYaml());
            previewArea.setCaretPosition(0);
            if (loadIntoEditorAction != null) {
                loadIntoEditorAction.run();
            }
        } catch (Exception ex) {
            showError(ex, true);
        }
    }

    public String generateYaml() {
        return getSelectedKind() == RuleKind.CONLLU ? generateConlluYaml() : generateCorrectionYaml();
    }

    private String generateCorrectionYaml() {
        String action = String.valueOf(correctionActionBox.getSelectedItem());
        validateCorrectionState(action);

        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> rule = baseRuleMap();
        Map<String, Object> rewrite = new LinkedHashMap<>();
        Map<String, Object> merge = new LinkedHashMap<>();

        Map<String, Object> match = buildCorrectionMatch();
        if (!match.isEmpty()) {
            rewrite.put("match", match);
        }
        if (useBetweenLengthBox.isSelected()) {
            rewrite.put("between", Map.of("length", betweenLengthSpinner.getValue()));
        }
        putIfNotBlank(rule, "targets", targetsField.getText());

        switch (action) {
            case "rewrite_before_after" -> {
                putCsvIfPresent(rewrite, "before", surfaceBeforeField.getText());
                putCsvIfPresent(rewrite, "after", surfaceAfterField.getText());
                Map<String, Object> gloss = new LinkedHashMap<>();
                putCsvIfPresent(gloss, "before", glossBeforeField.getText());
                putCsvIfPresent(gloss, "after", glossAfterField.getText());
                if (!gloss.isEmpty()) {
                    rewrite.put("gloss", gloss);
                }
            }
            case "rewrite_gloss_only" -> {
                Map<String, Object> gloss = new LinkedHashMap<>();
                gloss.put("before", csvRequired(glossBeforeField.getText(), "Gloss before cannot be blank"));
                gloss.put("after", csvRequired(glossAfterField.getText(), "Gloss after cannot be blank"));
                rewrite.put("gloss", gloss);
            }
            case "regex_sub" -> {
                Map<String, Object> regexSub = new LinkedHashMap<>();
                regexSub.put("scope", regexScopeBox.getSelectedItem());
                regexSub.put("pattern", require(regexPatternField.getText(), "Regex pattern cannot be blank").trim());
                regexSub.put("repl", nullToEmpty(regexReplacementField.getText()).trim());
                if (regexIgnoreCaseBox.isSelected()) {
                    regexSub.put("ignore_case", true);
                }
                rewrite.put("regex_sub", regexSub);
            }
            case "split_suffix" -> {
                Map<String, Object> split = new LinkedHashMap<>();
                split.put("type", "suffix");
                split.put("suffixes", csvRequired(splitSuffixesField.getText(), "Suffixes cannot be blank"));
                split.put("gloss_placement", glossPlacementBox.getSelectedItem());
                rewrite.put("split", split);
            }
            case "split_suffix_with_final_gloss" -> {
                Map<String, Object> split = new LinkedHashMap<>();
                split.put("type", "suffix_with_final_gloss");
                split.put("suffixes", csvRequired(splitSuffixesField.getText(), "Suffixes cannot be blank"));
                if (!splitGlossLastStartsWithPanel.getValues().isEmpty()) {
                    split.put("gloss_last_match", Map.of("starts_with", splitGlossLastStartsWithPanel.getValues()));
                }
                rewrite.put("split", split);
            }
            case "delete_chars" -> rewrite.put("delete", Map.of(
                    "type", "chars",
                    "chars", csvRequired(deleteValuesField.getText(), "Delete chars cannot be blank")
            ));
            case "delete_part" -> rewrite.put("delete", Map.of(
                    "type", "part",
                    "chars", csvRequired(deleteValuesField.getText(), "Delete parts cannot be blank")
            ));
            case "insert_segment" -> rewrite.put("insert", Map.of(
                    "segment", require(insertSegmentField.getText(), "Insert segment cannot be blank").trim(),
                    "token", insertTokenSpinner.getValue(),
                    "position", insertPositionSpinner.getValue()
            ));
            case "merge_tokens" -> {
                List<List<String>> seqs = mergeSequencesPanel.getSequences();
                if (seqs.isEmpty()) {
                    throw new IllegalArgumentException("At least one merge token sequence is required.");
                }
                merge.put("match", Map.of("tokens", seqs));
            }
            default -> throw new IllegalStateException("Unsupported correction action: " + action);
        }

        if (!rewrite.isEmpty()) {
            rule.put("rewrite", rewrite);
        }
        if (!merge.isEmpty()) {
            rule.put("merge", merge);
        }

        root.put("rules", List.of(rule));
        return yaml.dump(root);
    }

    private void validateCorrectionState(String action) {
        boolean hasTokenSequences = !matchTokenSequencesPanel.getSequences().isEmpty();
        boolean hasTokenSelectors = hasAnyTokenSelectors();
        boolean hasSurfaceMatch = !nullToEmpty(surfaceSideField.getText()).isBlank()
                || !nullToEmpty(surfaceRootInLexiconField.getText()).isBlank()
                || surfaceRootStartsWithVowelBox.isSelected();
        boolean hasBetweenOrTargets = useBetweenLengthBox.isSelected() || !nullToEmpty(targetsField.getText()).isBlank();
        boolean hasGlossMatch = !matchGlossPanel.getValues().isEmpty()
                || !matchGlossStartsWithPanel.getValues().isEmpty()
                || !nullToEmpty(matchGlossInLexiconField.getText()).isBlank();

        if (!"merge_tokens".equals(action) && hasTokenSequences && hasTokenSelectors) {
            throw new IllegalArgumentException("Use either token selector fields or token sequences, not both.");
        }

        if ("merge_tokens".equals(action) && (hasTokenSequences || hasTokenSelectors || hasSurfaceMatch || hasBetweenOrTargets || hasGlossMatch)) {
            throw new IllegalArgumentException("Merge rules only support merge.match.tokens in the guided builder.");
        }

        if ("rewrite_before_after".equals(action)) {
            boolean hasSurfaceRewrite = !csv(surfaceBeforeField.getText()).isEmpty() || !csv(surfaceAfterField.getText()).isEmpty();
            boolean hasGlossRewrite = !csv(glossBeforeField.getText()).isEmpty() || !csv(glossAfterField.getText()).isEmpty();

            if (!hasSurfaceRewrite && !hasGlossRewrite) {
                throw new IllegalArgumentException("Provide at least one surface or gloss rewrite.");
            }
            if (hasSurfaceRewrite && (csv(surfaceBeforeField.getText()).isEmpty() || csv(surfaceAfterField.getText()).isEmpty())) {
                throw new IllegalArgumentException("Surface rewrite needs both 'before' and 'after'.");
            }
            if (hasGlossRewrite && (csv(glossBeforeField.getText()).isEmpty() || csv(glossAfterField.getText()).isEmpty())) {
                throw new IllegalArgumentException("Gloss rewrite needs both 'before' and 'after'.");
            }
        }
    }

    private boolean hasAnyTokenSelectors() {
        return !matchTokensIswordPanel.getValues().isEmpty()
                || !matchTokensAnyPanel.getValues().isEmpty()
                || !matchTokensStartsWithPanel.getValues().isEmpty()
                || !matchTokensEndsWithPanel.getValues().isEmpty()
                || !matchTokensHasSegmentPanel.getValues().isEmpty()
                || matchStartsWithVowelBox.isSelected();
    }

    private Map<String, Object> buildCorrectionMatch() {
        Map<String, Object> match = new LinkedHashMap<>();

        List<String> glossValues = matchGlossPanel.getValues();
        List<String> glossStartsWith = matchGlossStartsWithPanel.getValues();
        String glossLexicon = nullToEmpty(matchGlossInLexiconField.getText()).trim();

        boolean hasStructuredGloss =
                !glossStartsWith.isEmpty()
                        || !glossLexicon.isBlank();

        if (hasStructuredGloss) {
            Map<String, Object> gloss = new LinkedHashMap<>();
            putListIfPresent(gloss, "any", glossValues);
            putListIfPresent(gloss, "starts_with", glossStartsWith);
            putIfNotBlank(gloss, "in_lexicon", glossLexicon);
            if (!gloss.isEmpty()) {
                match.put("gloss", gloss);
            }
        } else if (!glossValues.isEmpty()) {
            match.put("gloss", new ArrayList<>(glossValues));
        }

        Map<String, Object> tokens = new LinkedHashMap<>();
        putListIfPresent(tokens, "isword", matchTokensIswordPanel.getValues());
        putListIfPresent(tokens, "any", matchTokensAnyPanel.getValues());
        putListIfPresent(tokens, "startswith", matchTokensStartsWithPanel.getValues());
        putListIfPresent(tokens, "endswith", matchTokensEndsWithPanel.getValues());
        putListIfPresent(tokens, "has_segment", matchTokensHasSegmentPanel.getValues());
        if (matchStartsWithVowelBox.isSelected()) {
            tokens.put("startswith_vowel", true);
        }
        if (!tokens.isEmpty()) {
            match.put("tokens", tokens);
        } else if (!matchTokenSequencesPanel.getSequences().isEmpty()) {
            match.put("tokens", matchTokenSequencesPanel.getSequences());
        }

        Map<String, Object> surface = new LinkedHashMap<>();
        putIfNotBlank(surface, "side", surfaceSideField.getText());
        putIfNotBlank(surface, "root_in_lexicon", surfaceRootInLexiconField.getText());
        if (surfaceRootStartsWithVowelBox.isSelected()) {
            surface.put("root_startswith_vowel", true);
        }
        if (!surface.isEmpty()) {
            match.put("surface", surface);
        }

        return match;
    }

    private String generateConlluYaml() {
        String mode = String.valueOf(conlluModeBox.getSelectedItem());
        validateConlluState(mode);

        Map<String, Object> root = new LinkedHashMap<>();

        if ("global_config".equals(mode) || "rule_full".equals(mode)) {
            fillConlluGlobals(root);
        }

        if (!"global_config".equals(mode)) {
            Map<String, Object> rule = new LinkedHashMap<>();
            rule.put("name", require(nameField.getText(), "Rule name cannot be blank").trim());
            putIfNotBlank(rule, "id", idField.getText());
            putIfNotBlank(rule, "description", descriptionArea.getText());
            rule.put("scope", conlluScopeBox.getSelectedItem());
            if (((Number) conlluPrioritySpinner.getValue()).intValue() != 0) {
                rule.put("priority", conlluPrioritySpinner.getValue());
            }

            Map<String, Object> match = buildConlluMatch();
            if (!match.isEmpty()) {
                rule.put("match", match);
            }

            Map<String, Object> set = buildConlluSet(mode);
            if (!set.isEmpty()) {
                rule.put("set", set);
            }

            root.put("rules", List.of(rule));
        }

        return yaml.dump(root);
    }

    private void validateConlluState(String mode) {
        boolean hasLiteralGloss = !nullToEmpty(conlluMatchGlossLiteralField.getText()).isBlank();
        boolean hasStructuredGloss =
                !nullToEmpty(conlluMatchGlossRegexField.getText()).isBlank()
                        || !nullToEmpty(conlluMatchGlossInLexiconField.getText()).isBlank()
                        || !conlluMatchGlossInListPanel.getValues().isEmpty()
                        || !conlluMatchGlossRequirePanel.getValues().isEmpty()
                        || !conlluMatchGlossForbidPanel.getValues().isEmpty()
                        || !conlluMatchExtractPanel.getSpecs().isEmpty();

        if (hasLiteralGloss && hasStructuredGloss) {
            throw new IllegalArgumentException("Choose either a literal gloss match or structured gloss criteria, not both.");
        }

        if ("global_config".equals(mode) && !hasAnyConlluGlobals()) {
            throw new IllegalArgumentException("Global config cannot be empty.");
        }

        if ("rule_upos".equals(mode) && nullToEmpty(conlluSetUposField.getText()).isBlank()) {
            throw new IllegalArgumentException("set.upos is required for rule_upos.");
        }

        if ("rule_feats".equals(mode)
                && conlluFeatsPanel.getMap().isEmpty()
                && conlluFeatsTemplatePanel.getMap().isEmpty()) {
            throw new IllegalArgumentException("rule_feats requires set.feats or set.feats_template.");
        }

        if ("rule_extract".equals(mode) && conlluSetExtractPanel.getSpecs().isEmpty()) {
            throw new IllegalArgumentException("rule_extract requires at least one set.extract row.");
        }

        if ("rule_full".equals(mode)
                && nullToEmpty(conlluSetUposField.getText()).isBlank()
                && conlluFeatsPanel.getMap().isEmpty()
                && conlluFeatsTemplatePanel.getMap().isEmpty()
                && conlluSetExtractPanel.getSpecs().isEmpty()) {
            throw new IllegalArgumentException("rule_full requires at least one set.upos, set.feats, set.feats_template or set.extract.");
        }
    }

    private boolean hasAnyConlluGlobals() {
        return !defPosPanel.getValues().isEmpty()
                || !defFeatsPanel.getValues().isEmpty()
                || !nullToEmpty(lexiconNameField.getText()).isBlank()
                || !nullToEmpty(lexiconFileField.getText()).isBlank()
                || !glossMapPosPanel.getMap().isEmpty()
                || !glossMapFeatsPanel.getEntries().isEmpty()
                || !nullToEmpty(extractorNameField.getText()).isBlank()
                || !extractorsFilePanel.getValues().isEmpty()
                || !rulesFilePanel.getValues().isEmpty();
    }

    private Map<String, Object> buildConlluMatch() {
        Map<String, Object> match = new LinkedHashMap<>();
        putIfNotBlank(match, "regex", conlluMatchRegexField.getText());
        putListIfPresent(match, "in_list", conlluMatchInListPanel.getValues());
        putListIfPresent(match, "require", conlluMatchRequirePanel.getValues());
        putListIfPresent(match, "forbid", conlluMatchForbidPanel.getValues());

        if (!nullToEmpty(conlluMatchGlossLiteralField.getText()).isBlank()) {
            match.put("gloss", conlluMatchGlossLiteralField.getText().trim());
        } else {
            Map<String, Object> gloss = new LinkedHashMap<>();
            putIfNotBlank(gloss, "regex", conlluMatchGlossRegexField.getText());
            putIfNotBlank(gloss, "in_lexicon", conlluMatchGlossInLexiconField.getText());
            putListIfPresent(gloss, "in_list", conlluMatchGlossInListPanel.getValues());
            putListIfPresent(gloss, "require", conlluMatchGlossRequirePanel.getValues());
            putListIfPresent(gloss, "forbid", conlluMatchGlossForbidPanel.getValues());
            if (!conlluMatchExtractPanel.getSpecs().isEmpty()) {
                gloss.put("extract", conlluMatchExtractPanel.getSpecs());
            }
            if (!gloss.isEmpty()) {
                match.put("gloss", gloss);
            }
        }

        return match;
    }

    private Map<String, Object> buildConlluSet(String mode) {
        Map<String, Object> set = new LinkedHashMap<>();

        if ("rule_upos".equals(mode) || "rule_full".equals(mode)) {
            putIfNotBlank(set, "upos", conlluSetUposField.getText());
        }

        if ("rule_feats".equals(mode) || "rule_full".equals(mode)) {
            if (!conlluFeatsPanel.getMap().isEmpty()) {
                set.put("feats", conlluFeatsPanel.getMap());
            }
            if (!conlluFeatsTemplatePanel.getMap().isEmpty()) {
                set.put("feats_template", conlluFeatsTemplatePanel.getMap());
            }
        }

        if ("rule_extract".equals(mode) || "rule_full".equals(mode)) {
            if (!conlluSetExtractPanel.getSpecs().isEmpty()) {
                set.put("extract", conlluSetExtractPanel.getSpecs());
            }
        }

        return set;
    }

    private void fillConlluGlobals(Map<String, Object> root) {
        Map<String, Object> def = new LinkedHashMap<>();
        putListIfPresent(def, "pos", defPosPanel.getValues());
        putListIfPresent(def, "feats", defFeatsPanel.getValues());
        if (!def.isEmpty()) {
            root.put("def", def);
        }

        if (!nullToEmpty(lexiconNameField.getText()).isBlank() && !nullToEmpty(lexiconFileField.getText()).isBlank()) {
            root.put("lexicons", Map.of(lexiconNameField.getText().trim(), lexiconFileField.getText().trim()));
        }

        Map<String, Object> glossMap = new LinkedHashMap<>();
        if (!glossMapPosPanel.getMap().isEmpty()) {
            List<Map<String, Object>> posEntries = new ArrayList<>();
            for (var e : glossMapPosPanel.getMap().entrySet()) {
                posEntries.add(Map.of(e.getKey(), e.getValue()));
            }
            glossMap.put("pos", posEntries);
        }
        if (!glossMapFeatsPanel.getEntries().isEmpty()) {
            glossMap.put("feats", glossMapFeatsPanel.getEntries());
        }
        if (!glossMap.isEmpty()) {
            root.put("gloss_map", glossMap);
        }

        if (!nullToEmpty(extractorNameField.getText()).isBlank()) {
            Map<String, Object> extractors = new LinkedHashMap<>();
            Map<String, Object> extractor = new LinkedHashMap<>();

            Map<String, Object> tagSchema = new LinkedHashMap<>();
            Map<String, Object> series = new LinkedHashMap<>();
            for (var e : extractorSeriesPanel.getMap().entrySet()) {
                series.put(e.getKey(), Map.of("role", e.getValue()));
            }

            Map<String, Object> values = new LinkedHashMap<>();
            if (!extractorPersonsPanel.getValues().isEmpty()) {
                values.put("person", new ArrayList<>(extractorPersonsPanel.getValues()));
            }
            if (!nullToEmpty(extractorNumberSuffixField.getText()).isBlank()) {
                values.put("number", Map.of("suffix", extractorNumberSuffixField.getText().trim()));
            }

            if (!series.isEmpty()) {
                tagSchema.put("series", series);
            }
            if (!values.isEmpty()) {
                tagSchema.put("values", values);
            }
            if (!tagSchema.isEmpty()) {
                extractor.put("tag_schema", tagSchema);
            }

            if (!routingRulePanel.getRoutingRules().isEmpty()) {
                extractor.put("routing", routingRulePanel.getRoutingRules());
            }

            extractors.put(extractorNameField.getText().trim(), extractor);
            root.put("extractors", extractors);
        }

        putListIfPresent(root, "extractors_file", extractorsFilePanel.getValues());
        putListIfPresent(root, "rules_file", rulesFilePanel.getValues());
    }

    private void chooseFileInto(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            field.setText(path.toString());
        }
    }

    private Map<String, Object> baseRuleMap() {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("id", require(idField.getText(), "Rule id cannot be blank").trim());
        rule.put("name", require(nameField.getText(), "Rule name cannot be blank").trim());
        putIfNotBlank(rule, "description", descriptionArea.getText());
        return rule;
    }

    private void clearError() {
        errorLabel.setText(" ");
    }

    private void showError(Throwable throwable, boolean dialog) {
        String msg = throwable == null ? "Unknown error" : nullToEmpty(throwable.getMessage()).trim();
        if (msg.isBlank()) {
            msg = throwable == null ? "Unknown error" : throwable.getClass().getSimpleName();
        }
        errorLabel.setText(msg);
        if (dialog) {
            Dialogs.error(this, "Builder error", throwable);
        }
    }

    private static GridBagConstraints baseConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 0;
        return c;
    }

    private static void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component component) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(component, c);
    }

    private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value.trim());
        }
    }

    private static void putListIfPresent(Map<String, Object> map, String key, List<String> values) {
        if (values != null && !values.isEmpty()) {
            map.put(key, new ArrayList<>(values));
        }
    }

    private static void putCsvIfPresent(Map<String, Object> map, String key, String text) {
        List<String> values = csv(text);
        if (!values.isEmpty()) {
            map.put(key, new ArrayList<>(values));
        }
    }

    private static String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static List<String> csv(String text) {
        String value = nullToEmpty(text).trim();
        if (value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("\\s*,\\s*")).stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static List<String> csvRequired(String text, String message) {
        List<String> values = csv(text);
        if (values.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return values;
    }
}