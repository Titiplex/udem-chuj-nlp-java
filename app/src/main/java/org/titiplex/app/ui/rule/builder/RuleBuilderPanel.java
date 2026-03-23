package org.titiplex.app.ui.rule.builder;

import org.titiplex.app.persistence.entity.RuleKind;
import org.titiplex.app.ui.common.Dialogs;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public final class RuleBuilderPanel extends JPanel {
    private final JComboBox<RuleKind> kindBox = new JComboBox<>(RuleKind.values());
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(3, 60);

    private final JLabel errorLabel = new JLabel(" ");
    private final JTextArea previewArea = new JTextArea(20, 80);

    private final CardLayout rootCardLayout = new CardLayout();
    private final JPanel rootCards = new JPanel(rootCardLayout);

    // ---------- correction ----------
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

    private final JComboBox<String> correctionRegexScopeBox = new JComboBox<>(new String[]{
            "chuj", "gloss"
    });
    private final JCheckBox correctionRegexIgnoreCaseBox = new JCheckBox("Ignore case");

    private final JTextField correctionBeforeField = new JTextField();
    private final JTextField correctionAfterField = new JTextField();
    private final JTextField correctionGlossBeforeField = new JTextField();
    private final JTextField correctionGlossAfterField = new JTextField();

    private final JTextField correctionRegexPatternField = new JTextField();
    private final JTextField correctionRegexReplacementField = new JTextField();

    private final JComboBox<String> correctionSplitTypeBox = new JComboBox<>(new String[]{
            "suffix", "end", "suffix_with_final_gloss"
    });
    private final JTextField correctionSplitSuffixesField = new JTextField();
    private final JComboBox<String> correctionGlossPlacementBox = new JComboBox<>(new String[]{
            "right", "left"
    });
    private final JTextField correctionGlossLastStartsWithField = new JTextField();

    private final JComboBox<String> correctionDeleteTypeBox = new JComboBox<>(new String[]{
            "chars", "part"
    });
    private final JTextField correctionDeleteCharsField = new JTextField();

    private final JTextField correctionInsertSegmentField = new JTextField();
    private final JSpinner correctionInsertTokenSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JSpinner correctionInsertPositionSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

    private final JTextArea correctionMergeTokenSequencesArea = new JTextArea(4, 40);

    // correction match
    private final JTextField correctionMatchGlossField = new JTextField();
    private final JTextField correctionMatchGlossStartsWithField = new JTextField();
    private final JTextField correctionMatchGlossInLexiconField = new JTextField();

    private final JTextField correctionMatchTokenIswordField = new JTextField();
    private final JTextField correctionMatchTokenAnyField = new JTextField();
    private final JTextField correctionMatchTokenStartsWithField = new JTextField();
    private final JTextField correctionMatchTokenEndsWithField = new JTextField();
    private final JTextField correctionMatchTokenHasSegmentField = new JTextField();
    private final JCheckBox correctionMatchStartsWithVowelBox = new JCheckBox("tokens.startswith_vowel");

    private final JTextArea correctionMatchTokenSequencesArea = new JTextArea(4, 40);

    private final JTextField correctionSurfaceSideField = new JTextField();
    private final JTextField correctionSurfaceRootInLexiconField = new JTextField();
    private final JCheckBox correctionSurfaceRootStartsWithVowelBox = new JCheckBox("surface.root_startswith_vowel");

    private final JSpinner correctionBetweenLengthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JCheckBox correctionUseBetweenLengthBox = new JCheckBox("Use between.length");
    private final JTextField correctionTargetsField = new JTextField();

    private final JPanel correctionRewritePanel = new JPanel(new GridBagLayout());
    private final JPanel correctionRegexPanel = new JPanel(new GridBagLayout());
    private final JPanel correctionSplitPanel = new JPanel(new GridBagLayout());
    private final JPanel correctionDeletePanel = new JPanel(new GridBagLayout());
    private final JPanel correctionInsertPanel = new JPanel(new GridBagLayout());
    private final JPanel correctionMergePanel = new JPanel(new GridBagLayout());

    // ---------- conllu ----------
    private final JComboBox<String> conlluModeBox = new JComboBox<>(new String[]{
            "rule_upos",
            "rule_feats",
            "rule_extract",
            "rule_full",
            "global_config"
    });

    private final JComboBox<String> conlluScopeBox = new JComboBox<>(new String[]{
            "token", "morpheme"
    });
    private final JSpinner conlluPrioritySpinner = new JSpinner(new SpinnerNumberModel(0, -100, 1000, 1));

    private final JTextField conlluMatchRegexField = new JTextField();
    private final JTextField conlluMatchInListField = new JTextField();
    private final JTextField conlluMatchRequireField = new JTextField();
    private final JTextField conlluMatchForbidField = new JTextField();

    private final JTextField conlluMatchGlossLiteralField = new JTextField();
    private final JTextField conlluMatchGlossRegexField = new JTextField();
    private final JTextField conlluMatchGlossInLexiconField = new JTextField();
    private final JTextField conlluMatchGlossInListField = new JTextField();
    private final JTextField conlluMatchGlossRequireField = new JTextField();
    private final JTextField conlluMatchGlossForbidField = new JTextField();

    private final JTextArea conlluMatchExtractArea = new JTextArea(4, 40);

    private final JTextField conlluSetUposField = new JTextField();
    private final JTextArea conlluFeatsArea = new JTextArea(5, 40);
    private final JTextArea conlluFeatsTemplateArea = new JTextArea(5, 40);
    private final JTextArea conlluSetExtractArea = new JTextArea(4, 40);

    // globals
    private final JTextField conlluLexiconNameField = new JTextField();
    private final JTextField conlluLexiconFileField = new JTextField();
    private final JTextArea conlluDefPosArea = new JTextArea(4, 40);
    private final JTextArea conlluDefFeatsArea = new JTextArea(4, 40);
    private final JTextArea conlluGlossMapPosArea = new JTextArea(5, 40);
    private final JTextArea conlluGlossMapFeatsArea = new JTextArea(5, 40);
    private final JTextField conlluExtractorNameField = new JTextField("agreement_verbs");
    private final JTextArea conlluExtractorSeriesArea = new JTextArea(4, 40);
    private final JTextArea conlluExtractorPersonsArea = new JTextArea(3, 40);
    private final JTextField conlluExtractorNumberSuffixField = new JTextField("PL");
    private final JTextArea conlluExtractorRoutingArea = new JTextArea(5, 40);
    private final JTextArea conlluExtractorsFileArea = new JTextArea(4, 40);
    private final JTextArea conlluRulesFileArea = new JTextArea(4, 40);

    private final JPanel conlluRulePanel = new JPanel(new GridBagLayout());
    private final JPanel conlluGlobalPanel = new JPanel(new GridBagLayout());

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

        correctionMergeTokenSequencesArea.setLineWrap(true);
        correctionMergeTokenSequencesArea.setWrapStyleWord(true);
        correctionMatchTokenSequencesArea.setLineWrap(true);
        correctionMatchTokenSequencesArea.setWrapStyleWord(true);

        conlluMatchExtractArea.setLineWrap(true);
        conlluFeatsArea.setLineWrap(true);
        conlluFeatsTemplateArea.setLineWrap(true);
        conlluSetExtractArea.setLineWrap(true);
        conlluDefPosArea.setLineWrap(true);
        conlluDefFeatsArea.setLineWrap(true);
        conlluGlossMapPosArea.setLineWrap(true);
        conlluGlossMapFeatsArea.setLineWrap(true);
        conlluExtractorSeriesArea.setLineWrap(true);
        conlluExtractorPersonsArea.setLineWrap(true);
        conlluExtractorRoutingArea.setLineWrap(true);
        conlluExtractorsFileArea.setLineWrap(true);
        conlluRulesFileArea.setLineWrap(true);

        errorLabel.setForeground(new Color(180, 60, 60));

        buildCorrectionPanels();
        buildConlluPanels();

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
        previewButton.addActionListener(event -> safelyGeneratePreview(true));
        JButton loadButton = new JButton("Load into YAML editor");
        loadButton.addActionListener(event -> safelyLoadIntoEditor());

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
        splitPane.setResizeWeight(0.70);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        kindBox.addActionListener(event -> refreshFieldVisibility());
        correctionActionBox.addActionListener(event -> refreshFieldVisibility());
        correctionSplitTypeBox.addActionListener(event -> refreshFieldVisibility());
        correctionDeleteTypeBox.addActionListener(event -> refreshFieldVisibility());
        conlluModeBox.addActionListener(event -> refreshFieldVisibility());

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
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Correction rule"));
        GridBagConstraints c = baseConstraints();

        addRow(panel, c, 0, "Action", correctionActionBox);
        addCorrectionMatchPanel(panel, c, 1);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        panel.add(correctionRewritePanel, c);

        c.gridy = 3;
        panel.add(correctionRegexPanel, c);

        c.gridy = 4;
        panel.add(correctionSplitPanel, c);

        c.gridy = 5;
        panel.add(correctionDeletePanel, c);

        c.gridy = 6;
        panel.add(correctionInsertPanel, c);

        c.gridy = 7;
        panel.add(correctionMergePanel, c);

        c.gridy = 8;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), c);

        return panel;
    }

    private JPanel buildConlluCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("CoNLL-U"));
        GridBagConstraints c = baseConstraints();

        addRow(panel, c, 0, "Mode", conlluModeBox);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        panel.add(conlluRulePanel, c);

        c.gridy = 2;
        panel.add(conlluGlobalPanel, c);

        c.gridy = 3;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), c);

        return panel;
    }

    private void buildCorrectionPanels() {
        correctionRewritePanel.setBorder(BorderFactory.createTitledBorder("Rewrite"));
        GridBagConstraints c1 = baseConstraints();
        addRow(correctionRewritePanel, c1, 0, "Surface before (csv)", correctionBeforeField);
        addRow(correctionRewritePanel, c1, 1, "Surface after (csv)", correctionAfterField);
        addRow(correctionRewritePanel, c1, 2, "Gloss before (csv)", correctionGlossBeforeField);
        addRow(correctionRewritePanel, c1, 3, "Gloss after (csv)", correctionGlossAfterField);

        correctionRegexPanel.setBorder(BorderFactory.createTitledBorder("Regex"));
        GridBagConstraints c2 = baseConstraints();
        addRow(correctionRegexPanel, c2, 0, "Scope", correctionRegexScopeBox);
        addRow(correctionRegexPanel, c2, 1, "Pattern", correctionRegexPatternField);
        addRow(correctionRegexPanel, c2, 2, "Replacement", correctionRegexReplacementField);
        addRow(correctionRegexPanel, c2, 3, "", correctionRegexIgnoreCaseBox);

        correctionSplitPanel.setBorder(BorderFactory.createTitledBorder("Split"));
        GridBagConstraints c3 = baseConstraints();
        addRow(correctionSplitPanel, c3, 0, "Split type", correctionSplitTypeBox);
        addRow(correctionSplitPanel, c3, 1, "Suffixes / tokens (csv)", correctionSplitSuffixesField);
        addRow(correctionSplitPanel, c3, 2, "Gloss placement", correctionGlossPlacementBox);
        addRow(correctionSplitPanel, c3, 3, "gloss_last_match.starts_with (csv)", correctionGlossLastStartsWithField);

        correctionDeletePanel.setBorder(BorderFactory.createTitledBorder("Delete"));
        GridBagConstraints c4 = baseConstraints();
        addRow(correctionDeletePanel, c4, 0, "Delete type", correctionDeleteTypeBox);
        addRow(correctionDeletePanel, c4, 1, "Chars / parts (csv)", correctionDeleteCharsField);

        correctionInsertPanel.setBorder(BorderFactory.createTitledBorder("Insert"));
        GridBagConstraints c5 = baseConstraints();
        addRow(correctionInsertPanel, c5, 0, "Segment", correctionInsertSegmentField);
        addRow(correctionInsertPanel, c5, 1, "Token index", correctionInsertTokenSpinner);
        addRow(correctionInsertPanel, c5, 2, "Position", correctionInsertPositionSpinner);

        correctionMergePanel.setBorder(BorderFactory.createTitledBorder("Merge"));
        GridBagConstraints c6 = baseConstraints();
        JScrollPane seqScroll = new JScrollPane(correctionMergeTokenSequencesArea);
        seqScroll.setPreferredSize(new Dimension(220, 90));
        addRow(correctionMergePanel, c6, 0, "Token sequences (one seq per line, csv)", seqScroll);
    }

    private void addCorrectionMatchPanel(JPanel parent, GridBagConstraints c, int row) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Match"));
        GridBagConstraints mc = baseConstraints();

        addRow(panel, mc, 0, "Gloss literal(s) (csv)", correctionMatchGlossField);
        addRow(panel, mc, 1, "gloss.starts_with (csv)", correctionMatchGlossStartsWithField);
        addRow(panel, mc, 2, "gloss.in_lexicon", correctionMatchGlossInLexiconField);

        addRow(panel, mc, 3, "tokens.isword (csv)", correctionMatchTokenIswordField);
        addRow(panel, mc, 4, "tokens.any (csv)", correctionMatchTokenAnyField);
        addRow(panel, mc, 5, "tokens.startswith (csv)", correctionMatchTokenStartsWithField);
        addRow(panel, mc, 6, "tokens.endswith (csv)", correctionMatchTokenEndsWithField);
        addRow(panel, mc, 7, "tokens.has_segment (csv)", correctionMatchTokenHasSegmentField);
        addRow(panel, mc, 8, "", correctionMatchStartsWithVowelBox);

        JScrollPane tokenSeqScroll = new JScrollPane(correctionMatchTokenSequencesArea);
        tokenSeqScroll.setPreferredSize(new Dimension(220, 90));
        addRow(panel, mc, 9, "token sequences (one seq per line, csv)", tokenSeqScroll);

        addRow(panel, mc, 10, "surface.side", correctionSurfaceSideField);
        addRow(panel, mc, 11, "surface.root_in_lexicon", correctionSurfaceRootInLexiconField);
        addRow(panel, mc, 12, "", correctionSurfaceRootStartsWithVowelBox);

        addRow(panel, mc, 13, "", correctionUseBetweenLengthBox);
        addRow(panel, mc, 14, "between.length", correctionBetweenLengthSpinner);
        addRow(panel, mc, 15, "targets", correctionTargetsField);

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        parent.add(panel, c);
    }

    private void buildConlluPanels() {
        conlluRulePanel.setBorder(BorderFactory.createTitledBorder("Rule"));
        GridBagConstraints c1 = baseConstraints();

        addRow(conlluRulePanel, c1, 0, "Scope", conlluScopeBox);
        addRow(conlluRulePanel, c1, 1, "Priority", conlluPrioritySpinner);
        addRow(conlluRulePanel, c1, 2, "match.regex", conlluMatchRegexField);
        addRow(conlluRulePanel, c1, 3, "match.in_list (csv or files)", conlluMatchInListField);
        addRow(conlluRulePanel, c1, 4, "match.require (csv)", conlluMatchRequireField);
        addRow(conlluRulePanel, c1, 5, "match.forbid (csv)", conlluMatchForbidField);

        addRow(conlluRulePanel, c1, 6, "match.gloss literal", conlluMatchGlossLiteralField);
        addRow(conlluRulePanel, c1, 7, "match.gloss.regex", conlluMatchGlossRegexField);
        addRow(conlluRulePanel, c1, 8, "match.gloss.in_lexicon", conlluMatchGlossInLexiconField);
        addRow(conlluRulePanel, c1, 9, "match.gloss.in_list (csv or files)", conlluMatchGlossInListField);
        addRow(conlluRulePanel, c1, 10, "match.gloss.require (csv)", conlluMatchGlossRequireField);
        addRow(conlluRulePanel, c1, 11, "match.gloss.forbid (csv)", conlluMatchGlossForbidField);

        JScrollPane matchExtractScroll = new JScrollPane(conlluMatchExtractArea);
        matchExtractScroll.setPreferredSize(new Dimension(220, 90));
        addRow(conlluRulePanel, c1, 12, "match.extract (type=...,key=... per line)", matchExtractScroll);

        addRow(conlluRulePanel, c1, 13, "set.upos", conlluSetUposField);

        JScrollPane featsScroll = new JScrollPane(conlluFeatsArea);
        featsScroll.setPreferredSize(new Dimension(220, 90));
        addRow(conlluRulePanel, c1, 14, "set.feats (key=value per line)", featsScroll);

        JScrollPane featsTplScroll = new JScrollPane(conlluFeatsTemplateArea);
        featsTplScroll.setPreferredSize(new Dimension(220, 90));
        addRow(conlluRulePanel, c1, 15, "set.feats_template (key=value per line)", featsTplScroll);

        JScrollPane setExtractScroll = new JScrollPane(conlluSetExtractArea);
        setExtractScroll.setPreferredSize(new Dimension(220, 90));
        addRow(conlluRulePanel, c1, 16, "set.extract (type=...,key=... per line)", setExtractScroll);

        conlluGlobalPanel.setBorder(BorderFactory.createTitledBorder("Global config"));
        GridBagConstraints c2 = baseConstraints();

        JPanel lexiconFilePanel = new JPanel(new BorderLayout(6, 0));
        lexiconFilePanel.add(conlluLexiconFileField, BorderLayout.CENTER);
        JButton browseLexiconButton = new JButton("Browse...");
        browseLexiconButton.addActionListener(event -> chooseFileInto(conlluLexiconFileField));
        lexiconFilePanel.add(browseLexiconButton, BorderLayout.EAST);

        addRow(conlluGlobalPanel, c2, 0, "lexicon name", conlluLexiconNameField);
        addRow(conlluGlobalPanel, c2, 1, "lexicon file", lexiconFilePanel);

        JScrollPane defPosScroll = new JScrollPane(conlluDefPosArea);
        defPosScroll.setPreferredSize(new Dimension(220, 70));
        addRow(conlluGlobalPanel, c2, 2, "def.pos (csv/lines)", defPosScroll);

        JScrollPane defFeatScroll = new JScrollPane(conlluDefFeatsArea);
        defFeatScroll.setPreferredSize(new Dimension(220, 70));
        addRow(conlluGlobalPanel, c2, 3, "def.feats (csv/lines)", defFeatScroll);

        JScrollPane gmPosScroll = new JScrollPane(conlluGlossMapPosArea);
        gmPosScroll.setPreferredSize(new Dimension(220, 90));
        addRow(conlluGlobalPanel, c2, 4, "gloss_map.pos (GLOSS=UPOS per line)", gmPosScroll);

        JScrollPane gmFeatScroll = new JScrollPane(conlluGlossMapFeatsArea);
        gmFeatScroll.setPreferredSize(new Dimension(220, 90));
        addRow(conlluGlobalPanel, c2, 5, "gloss_map.feats (GLOSS=Feat:Value per line)", gmFeatScroll);

        addRow(conlluGlobalPanel, c2, 6, "extractor name", conlluExtractorNameField);

        JScrollPane seriesScroll = new JScrollPane(conlluExtractorSeriesArea);
        seriesScroll.setPreferredSize(new Dimension(220, 70));
        addRow(conlluGlobalPanel, c2, 7, "extractor series (A=ERG per line)", seriesScroll);

        JScrollPane personsScroll = new JScrollPane(conlluExtractorPersonsArea);
        personsScroll.setPreferredSize(new Dimension(220, 55));
        addRow(conlluGlobalPanel, c2, 8, "extractor persons (csv)", personsScroll);

        addRow(conlluGlobalPanel, c2, 9, "extractor number suffix", conlluExtractorNumberSuffixField);

        JScrollPane routingScroll = new JScrollPane(conlluExtractorRoutingArea);
        routingScroll.setPreferredSize(new Dimension(220, 90));
        addRow(conlluGlobalPanel, c2, 10, "routing (when => key=value;key=value)", routingScroll);

        JScrollPane extractorsFileScroll = new JScrollPane(conlluExtractorsFileArea);
        extractorsFileScroll.setPreferredSize(new Dimension(220, 70));
        addRow(conlluGlobalPanel, c2, 11, "extractors_file", extractorsFileScroll);

        JScrollPane rulesFileScroll = new JScrollPane(conlluRulesFileArea);
        rulesFileScroll.setPreferredSize(new Dimension(220, 70));
        addRow(conlluGlobalPanel, c2, 12, "rules_file", rulesFileScroll);
    }

    private void refreshFieldVisibility() {
        RuleKind kind = getSelectedKind();
        rootCardLayout.show(rootCards, kind.name());

        String action = String.valueOf(correctionActionBox.getSelectedItem());
        correctionRewritePanel.setVisible("rewrite_before_after".equals(action) || "rewrite_gloss_only".equals(action));
        correctionRegexPanel.setVisible("regex_sub".equals(action));
        correctionSplitPanel.setVisible("split_suffix".equals(action) || "split_suffix_with_final_gloss".equals(action));
        correctionDeletePanel.setVisible("delete_chars".equals(action) || "delete_part".equals(action));
        correctionInsertPanel.setVisible("insert_segment".equals(action));
        correctionMergePanel.setVisible("merge_tokens".equals(action));

        boolean suffixFinal = "split_suffix_with_final_gloss".equals(action);
        correctionSplitTypeBox.setSelectedItem(suffixFinal ? "suffix_with_final_gloss" : "suffix");
        correctionGlossLastStartsWithField.setEnabled(suffixFinal);

        String mode = String.valueOf(conlluModeBox.getSelectedItem());
        conlluRulePanel.setVisible(!"global_config".equals(mode));
        conlluGlobalPanel.setVisible("global_config".equals(mode) || "rule_full".equals(mode));

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
        Map<String, Object> rule = baseRuleMap();

        Map<String, Object> rewrite = new LinkedHashMap<>();
        Map<String, Object> merge = new LinkedHashMap<>();
        Map<String, Object> match = buildCorrectionMatch();

        if (!match.isEmpty()) {
            rewrite.put("match", match);
        }

        String action = String.valueOf(correctionActionBox.getSelectedItem());

        switch (action) {
            case "rewrite_before_after" -> {
                putCsvIfPresent(rewrite, "before", correctionBeforeField.getText());
                putCsvIfPresent(rewrite, "after", correctionAfterField.getText());

                Map<String, Object> gloss = new LinkedHashMap<>();
                putCsvIfPresent(gloss, "before", correctionGlossBeforeField.getText());
                putCsvIfPresent(gloss, "after", correctionGlossAfterField.getText());
                if (!gloss.isEmpty()) {
                    rewrite.put("gloss", gloss);
                }

                if (!rewrite.containsKey("before") && !rewrite.containsKey("after") && !rewrite.containsKey("gloss")) {
                    throw new IllegalArgumentException("At least one rewrite field must be filled.");
                }
            }
            case "rewrite_gloss_only" -> {
                Map<String, Object> gloss = new LinkedHashMap<>();
                gloss.put("before", csvRequired(correctionGlossBeforeField.getText(), "Gloss before cannot be blank"));
                gloss.put("after", csvRequired(correctionGlossAfterField.getText(), "Gloss after cannot be blank"));
                rewrite.put("gloss", gloss);
            }
            case "regex_sub" -> {
                Map<String, Object> regexSub = new LinkedHashMap<>();
                regexSub.put("scope", String.valueOf(correctionRegexScopeBox.getSelectedItem()));
                regexSub.put("pattern", require(correctionRegexPatternField.getText(), "Regex pattern cannot be blank").trim());
                regexSub.put("repl", nullToEmpty(correctionRegexReplacementField.getText()).trim());
                if (correctionRegexIgnoreCaseBox.isSelected()) {
                    regexSub.put("ignore_case", true);
                }
                rewrite.put("regex_sub", regexSub);
            }
            case "split_suffix", "split_suffix_with_final_gloss" -> {
                Map<String, Object> split = new LinkedHashMap<>();
                split.put("type", String.valueOf(correctionSplitTypeBox.getSelectedItem()));
                split.put("suffixes", csvRequired(correctionSplitSuffixesField.getText(), "Suffixes cannot be blank"));
                if (!suffixFinalGlossStartsWith().isEmpty()) {
                    Map<String, Object> gl = new LinkedHashMap<>();
                    gl.put("starts_with", suffixFinalGlossStartsWith());
                    split.put("gloss_last_match", gl);
                }
                if (!nullToEmpty((String) correctionGlossPlacementBox.getSelectedItem()).isBlank()) {
                    split.put("gloss_placement", correctionGlossPlacementBox.getSelectedItem());
                }
                rewrite.put("split", split);
            }
            case "delete_chars", "delete_part" -> {
                Map<String, Object> delete = new LinkedHashMap<>();
                delete.put("type", String.valueOf(correctionDeleteTypeBox.getSelectedItem()));
                delete.put("chars", csvRequired(correctionDeleteCharsField.getText(), "Delete values cannot be blank"));
                rewrite.put("delete", delete);
            }
            case "insert_segment" -> {
                Map<String, Object> insert = new LinkedHashMap<>();
                insert.put("segment", require(correctionInsertSegmentField.getText(), "Insert segment cannot be blank").trim());
                insert.put("token", correctionInsertTokenSpinner.getValue());
                insert.put("position", correctionInsertPositionSpinner.getValue());
                rewrite.put("insert", insert);
            }
            case "merge_tokens" -> {
                List<List<String>> seqs = parseTokenSequenceLines(correctionMergeTokenSequencesArea.getText());
                if (seqs.isEmpty()) {
                    throw new IllegalArgumentException("At least one merge token sequence is required.");
                }
                Map<String, Object> mm = new LinkedHashMap<>();
                Map<String, Object> mmatch = new LinkedHashMap<>();
                mmatch.put("tokens", seqs);
                mm.put("match", mmatch);
                merge.putAll(mm);
            }
            default -> throw new IllegalStateException("Unsupported correction action: " + action);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        if (!merge.isEmpty()) {
            rule.put("merge", merge);
        }
        if (!rewrite.isEmpty()) {
            rule.put("rewrite", rewrite);
        }
        root.put("rules", List.of(rule));
        return yaml.dump(root);
    }

    private Map<String, Object> buildCorrectionMatch() {
        Map<String, Object> match = new LinkedHashMap<>();

        List<String> tokenSequencesFlat = csv(correctionMatchGlossField.getText());
        if (!tokenSequencesFlat.isEmpty()) {
            if (tokenSequencesFlat.size() == 1) {
                match.put("gloss", tokenSequencesFlat.getFirst());
            } else {
                match.put("gloss", new ArrayList<>(tokenSequencesFlat));
            }
        }

        Map<String, Object> tokenMap = new LinkedHashMap<>();
        putCsvIfPresent(tokenMap, "isword", correctionMatchTokenIswordField.getText());
        putCsvIfPresent(tokenMap, "any", correctionMatchTokenAnyField.getText());
        putCsvIfPresent(tokenMap, "startswith", correctionMatchTokenStartsWithField.getText());
        putCsvIfPresent(tokenMap, "endswith", correctionMatchTokenEndsWithField.getText());
        putCsvIfPresent(tokenMap, "has_segment", correctionMatchTokenHasSegmentField.getText());
        if (correctionMatchStartsWithVowelBox.isSelected()) {
            tokenMap.put("startswith_vowel", true);
        }

        List<List<String>> tokenSeqs = parseTokenSequenceLines(correctionMatchTokenSequencesArea.getText());
        if (!tokenSeqs.isEmpty()) {
            if (tokenMap.isEmpty() && tokenSeqs.size() == 1) {
                match.put("tokens", tokenSeqs.getFirst());
            } else if (!tokenSeqs.isEmpty()) {
                match.put("tokens", tokenSeqs);
            }
        }
        if (!tokenMap.isEmpty()) {
            match.put("tokens", tokenMap);
        }

        Map<String, Object> glossMap = new LinkedHashMap<>();
        putCsvIfPresent(glossMap, "starts_with", correctionMatchGlossStartsWithField.getText());
        putIfNotBlank(glossMap, "in_lexicon", correctionMatchGlossInLexiconField.getText());
        if (!glossMap.isEmpty()) {
            match.put("gloss", glossMap);
        }

        Map<String, Object> surfaceMap = new LinkedHashMap<>();
        putIfNotBlank(surfaceMap, "side", correctionSurfaceSideField.getText());
        putIfNotBlank(surfaceMap, "root_in_lexicon", correctionSurfaceRootInLexiconField.getText());
        if (correctionSurfaceRootStartsWithVowelBox.isSelected()) {
            surfaceMap.put("root_startswith_vowel", true);
        }
        if (!surfaceMap.isEmpty()) {
            match.put("surface", surfaceMap);
        }

        if (correctionUseBetweenLengthBox.isSelected()) {
            Map<String, Object> between = new LinkedHashMap<>();
            between.put("length", correctionBetweenLengthSpinner.getValue());
            match.put("between", between);
        }

        if (!nullToEmpty(correctionTargetsField.getText()).isBlank()) {
            match.put("targets", correctionTargetsField.getText().trim());
        }

        return match;
    }

    private String generateConlluYaml() {
        Map<String, Object> root = new LinkedHashMap<>();
        String mode = String.valueOf(conlluModeBox.getSelectedItem());

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

    private void fillConlluGlobals(Map<String, Object> root) {
        Map<String, Object> def = new LinkedHashMap<>();
        List<String> pos = csvOrLines(conlluDefPosArea.getText());
        List<String> feats = csvOrLines(conlluDefFeatsArea.getText());
        if (!pos.isEmpty()) def.put("pos", new ArrayList<>(pos));
        if (!feats.isEmpty()) def.put("feats", new ArrayList<>(feats));
        if (!def.isEmpty()) root.put("def", def);

        if (!nullToEmpty(conlluLexiconNameField.getText()).isBlank() && !nullToEmpty(conlluLexiconFileField.getText()).isBlank()) {
            Map<String, Object> lexicons = new LinkedHashMap<>();
            lexicons.put(conlluLexiconNameField.getText().trim(), conlluLexiconFileField.getText().trim());
            root.put("lexicons", lexicons);
        }

        Map<String, Object> glossMap = new LinkedHashMap<>();
        List<Map<String, Object>> posMaps = parseKeyValueMapLines(conlluGlossMapPosArea.getText());
        List<Map<String, Object>> featMaps = parseFeatGlossMapLines(conlluGlossMapFeatsArea.getText());
        if (!posMaps.isEmpty()) glossMap.put("pos", posMaps);
        if (!featMaps.isEmpty()) glossMap.put("feats", featMaps);
        if (!glossMap.isEmpty()) root.put("gloss_map", glossMap);

        if (!nullToEmpty(conlluExtractorNameField.getText()).isBlank()) {
            Map<String, Object> extractors = new LinkedHashMap<>();
            Map<String, Object> extractor = new LinkedHashMap<>();

            Map<String, Object> tagSchema = new LinkedHashMap<>();
            Map<String, Object> series = new LinkedHashMap<>();
            for (String line : nonBlankLines(conlluExtractorSeriesArea.getText())) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    series.put(parts[0].trim(), Map.of("role", parts[1].trim()));
                }
            }

            Map<String, Object> values = new LinkedHashMap<>();
            List<String> persons = csvOrLines(conlluExtractorPersonsArea.getText());
            if (!persons.isEmpty()) {
                values.put("person", new ArrayList<>(persons));
            }

            Map<String, Object> number = new LinkedHashMap<>();
            if (!nullToEmpty(conlluExtractorNumberSuffixField.getText()).isBlank()) {
                number.put("suffix", conlluExtractorNumberSuffixField.getText().trim());
            }
            if (!number.isEmpty()) values.put("number", number);
            if (!series.isEmpty()) tagSchema.put("series", series);
            if (!values.isEmpty()) tagSchema.put("values", values);
            if (!tagSchema.isEmpty()) extractor.put("tag_schema", tagSchema);

            List<Map<String, Object>> routing = parseRoutingLines(conlluExtractorRoutingArea.getText());
            if (!routing.isEmpty()) extractor.put("routing", routing);

            extractors.put(conlluExtractorNameField.getText().trim(), extractor);
            root.put("extractors", extractors);
        }

        List<String> extractorsFiles = nonBlankLines(conlluExtractorsFileArea.getText());
        if (!extractorsFiles.isEmpty()) {
            root.put("extractors_file", new ArrayList<>(extractorsFiles));
        }

        List<String> rulesFiles = nonBlankLines(conlluRulesFileArea.getText());
        if (!rulesFiles.isEmpty()) {
            root.put("rules_file", new ArrayList<>(rulesFiles));
        }
    }

    private Map<String, Object> buildConlluMatch() {
        Map<String, Object> match = new LinkedHashMap<>();

        putIfNotBlank(match, "regex", conlluMatchRegexField.getText());
        putCsvIfPresent(match, "in_list", conlluMatchInListField.getText());
        putCsvIfPresent(match, "require", conlluMatchRequireField.getText());
        putCsvIfPresent(match, "forbid", conlluMatchForbidField.getText());

        if (!nullToEmpty(conlluMatchGlossLiteralField.getText()).isBlank()) {
            match.put("gloss", conlluMatchGlossLiteralField.getText().trim());
        } else {
            Map<String, Object> gloss = new LinkedHashMap<>();
            putIfNotBlank(gloss, "regex", conlluMatchGlossRegexField.getText());
            putIfNotBlank(gloss, "in_lexicon", conlluMatchGlossInLexiconField.getText());
            putCsvIfPresent(gloss, "in_list", conlluMatchGlossInListField.getText());
            putCsvIfPresent(gloss, "require", conlluMatchGlossRequireField.getText());
            putCsvIfPresent(gloss, "forbid", conlluMatchGlossForbidField.getText());

            List<Map<String, String>> extracts = parseExtractLines(conlluMatchExtractArea.getText());
            if (!extracts.isEmpty()) {
                gloss.put("extract", extracts);
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
            Map<String, String> feats = parseSimpleKeyValueLines(conlluFeatsArea.getText());
            if (!feats.isEmpty()) {
                set.put("feats", feats);
            }

            Map<String, String> featTemplates = parseSimpleKeyValueLines(conlluFeatsTemplateArea.getText());
            if (!featTemplates.isEmpty()) {
                set.put("feats_template", featTemplates);
            }
        }

        if ("rule_extract".equals(mode) || "rule_full".equals(mode)) {
            List<Map<String, String>> extracts = parseExtractLines(conlluSetExtractArea.getText());
            if (!extracts.isEmpty()) {
                set.put("extract", extracts);
            }
        }

        return set;
    }

    private List<String> suffixFinalGlossStartsWith() {
        return csv(correctionGlossLastStartsWithField.getText());
    }

    private void chooseFileInto(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            field.setText(path.toString());
        }
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

    private Map<String, Object> baseRuleMap() {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("id", require(idField.getText(), "Rule id cannot be blank").trim());
        rule.put("name", require(nameField.getText(), "Rule name cannot be blank").trim());
        putIfNotBlank(rule, "description", descriptionArea.getText());
        return rule;
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
        c.weighty = 0;
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
        return Arrays.stream(value.split("\\s*,\\s*"))
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

    private static List<String> nonBlankLines(String text) {
        return nullToEmpty(text).lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static List<String> csvOrLines(String text) {
        List<String> lines = nonBlankLines(text);
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            out.addAll(csv(line));
        }
        return out;
    }

    private static Map<String, String> parseSimpleKeyValueLines(String text) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String line : nonBlankLines(text)) {
            int idx = line.indexOf('=');
            if (idx <= 0 || idx == line.length() - 1) continue;
            out.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
        }
        return out;
    }

    private static List<List<String>> parseTokenSequenceLines(String text) {
        List<List<String>> out = new ArrayList<>();
        for (String line : nonBlankLines(text)) {
            List<String> seq = csv(line);
            if (!seq.isEmpty()) {
                out.add(new ArrayList<>(seq));
            }
        }
        return out;
    }

    private static List<Map<String, String>> parseExtractLines(String text) {
        List<Map<String, String>> out = new ArrayList<>();
        for (String line : nonBlankLines(text)) {
            Map<String, String> entry = new LinkedHashMap<>();
            for (String piece : line.split("\\s*,\\s*")) {
                String[] kv = piece.split("=", 2);
                if (kv.length == 2) {
                    entry.put(kv[0].trim(), kv[1].trim());
                }
            }
            if (!entry.isEmpty()) {
                out.add(entry);
            }
        }
        return out;
    }

    private static List<Map<String, Object>> parseKeyValueMapLines(String text) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String line : nonBlankLines(text)) {
            int idx = line.indexOf('=');
            if (idx <= 0 || idx == line.length() - 1) continue;
            out.add(Map.of(line.substring(0, idx).trim(), line.substring(idx + 1).trim()));
        }
        return out;
    }

    private static List<Map<String, Object>> parseFeatGlossMapLines(String text) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String line : nonBlankLines(text)) {
            int idx = line.indexOf('=');
            if (idx <= 0 || idx == line.length() - 1) continue;
            String gloss = line.substring(0, idx).trim();
            String rhs = line.substring(idx + 1).trim();
            int idx2 = rhs.indexOf(':');
            if (idx2 <= 0 || idx2 == rhs.length() - 1) continue;
            out.add(Map.of(gloss, List.of(rhs.substring(0, idx2).trim(), rhs.substring(idx2 + 1).trim())));
        }
        return out;
    }

    private static List<Map<String, Object>> parseRoutingLines(String text) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String line : nonBlankLines(text)) {
            String[] parts = line.split("=>", 2);
            if (parts.length != 2) continue;
            Map<String, Object> rr = new LinkedHashMap<>();
            rr.put("when", parts[0].trim());

            Map<String, Object> set = new LinkedHashMap<>();
            for (String assign : parts[1].split("\\s*;\\s*")) {
                String[] kv = assign.split("=", 2);
                if (kv.length == 2) {
                    set.put(kv[0].trim(), kv[1].trim());
                }
            }
            rr.put("set", set);
            out.add(rr);
        }
        return out;
    }
}