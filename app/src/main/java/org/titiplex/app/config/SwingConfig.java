package org.titiplex.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.titiplex.app.service.*;
import org.titiplex.app.ui.frame.MainFrame;

@Configuration
public class SwingConfig {

    private final RuleService ruleService;
    private final CorrectedEntryService correctedEntryService;
    private final RawEntryService rawEntryService;
    private final CorpusImportService corpusImportService;
    private final AutoCorrectionService autoCorrectionService;
    private final DesktopExportService exportService;
    private final ConlluPreviewService conlluPreviewService;
    private final AnnotationConfigStateService annotationConfigStateService;

    public SwingConfig(
            RuleService ruleService,
            CorrectedEntryService correctedEntryService,
            RawEntryService rawEntryService,
            CorpusImportService corpusImportService,
            AutoCorrectionService autoCorrectionService,
            DesktopExportService exportService,
            ConlluPreviewService conlluPreviewService,
            AnnotationConfigStateService annotationConfigStateService
    ) {
        this.ruleService = ruleService;
        this.correctedEntryService = correctedEntryService;
        this.rawEntryService = rawEntryService;
        this.corpusImportService = corpusImportService;
        this.autoCorrectionService = autoCorrectionService;
        this.exportService = exportService;
        this.conlluPreviewService = conlluPreviewService;
        this.annotationConfigStateService = annotationConfigStateService;

    }
    @Bean
    public MainFrame mainFrame() {
        return new MainFrame(
                ruleService,
                correctedEntryService,
                rawEntryService,
                corpusImportService,
                autoCorrectionService,
                exportService,
                annotationConfigStateService,
                conlluPreviewService
                );
    }
}
