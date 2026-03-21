package org.titiplex.pipeline;

import org.titiplex.align.Tokenizer;
import org.titiplex.model.AlignedToken;
import org.titiplex.align.TokenAligner;
import org.titiplex.model.CorrectedBlock;
import org.titiplex.model.RawBlock;
import org.titiplex.rules.RuleEngine;

import java.util.List;
import java.util.stream.Collectors;

public final class CorrectionPipeline {
    private final TokenAligner tokenAligner;
    private final RuleEngine ruleEngine;

    public CorrectionPipeline(TokenAligner tokenAligner, RuleEngine ruleEngine) {
        this.tokenAligner = tokenAligner;
        this.ruleEngine = ruleEngine;
    }

    public CorrectedBlock process(RawBlock rawBlock) {
        List<String> chujWords = Tokenizer.tokenizeLine(rawBlock.chujText(), true);
        List<String> glossWords = Tokenizer.tokenizeLine(rawBlock.glossText(), false);

        List<AlignedToken> aligned = tokenAligner.align(chujWords, glossWords);
        List<AlignedToken> corrected = ruleEngine.apply(aligned);

        String correctedChuj = corrected.stream()
                .map(AlignedToken::chujSurface)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
        String correctedGloss = corrected.stream()
                .map(AlignedToken::glossSurface)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        return new CorrectedBlock(rawBlock.id(), correctedChuj, correctedGloss, rawBlock.translation(), corrected);
    }
}
