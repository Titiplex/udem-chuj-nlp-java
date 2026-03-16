package org.titiplex.align;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TokenAligner {
    private final int gapCostChuj;
    private final int gapCostGloss;

    public TokenAligner() {
        this(2, 2);
    }

    public TokenAligner(int gapCostChuj, int gapCostGloss) {
        this.gapCostChuj = gapCostChuj;
        this.gapCostGloss = gapCostGloss;
    }

    public List<AlignedToken> align(List<String> chujWords, List<String> glossWords) {
        int n = chujWords.size();
        int m = glossWords.size();

        int[][] dp = new int[n + 1][m + 1];
        AlignmentStep[][] back = new AlignmentStep[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            dp[i][0] = dp[i - 1][0] + gapCostChuj;
            back[i][0] = new AlignmentStep(i - 1, 0, AlignmentStep.StepType.DELETE_GLOSS);
        }
        for (int j = 1; j <= m; j++) {
            dp[0][j] = dp[0][j - 1] + gapCostGloss;
            back[0][j] = new AlignmentStep(0, j - 1, AlignmentStep.StepType.INSERT_GLOSS);
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int matchCost = dp[i - 1][j - 1] + Tokenizer.pairCost(chujWords.get(i - 1), glossWords.get(j - 1));
                int deleteGlossCost = dp[i - 1][j] + gapCostChuj;
                int insertGlossCost = dp[i][j - 1] + gapCostGloss;

                int best = Math.min(matchCost, Math.min(deleteGlossCost, insertGlossCost));
                dp[i][j] = best;

                // Better tie-break:
                // prefer MATCH only if strictly better,
                // otherwise prefer preserving unmatched gloss/chuj explicitly.
                if (matchCost < insertGlossCost && matchCost < deleteGlossCost) {
                    back[i][j] = new AlignmentStep(i - 1, j - 1, AlignmentStep.StepType.MATCH);
                } else if (insertGlossCost <= deleteGlossCost && insertGlossCost <= matchCost) {
                    back[i][j] = new AlignmentStep(i, j - 1, AlignmentStep.StepType.INSERT_GLOSS);
                } else if (deleteGlossCost <= insertGlossCost && deleteGlossCost <= matchCost) {
                    back[i][j] = new AlignmentStep(i - 1, j, AlignmentStep.StepType.DELETE_GLOSS);
                } else {
                    back[i][j] = new AlignmentStep(i - 1, j - 1, AlignmentStep.StepType.MATCH);
                }
            }
        }

        List<AlignedToken> aligned = new ArrayList<>();
        int i = n;
        int j = m;

        while (i > 0 || j > 0) {
            AlignmentStep step = back[i][j];
            if (step == null) {
                break;
            }

            switch (step.type()) {
                case MATCH -> {
                    aligned.add(AlignedToken.of(
                            chujWords.get(i - 1),
                            glossWords.get(j - 1),
                            Tokenizer.tokenizeWord(chujWords.get(i - 1)),
                            Tokenizer.tokenizeWord(glossWords.get(j - 1))
                    ));
                    i = step.previousI();
                    j = step.previousJ();
                }
                case DELETE_GLOSS -> {
                    aligned.add(AlignedToken.of(
                            chujWords.get(i - 1),
                            "",
                            Tokenizer.tokenizeWord(chujWords.get(i - 1)),
                            List.of()
                    ));
                    i = step.previousI();
                    j = step.previousJ();
                }
                case INSERT_GLOSS -> {
                    aligned.add(AlignedToken.of(
                            "",
                            glossWords.get(j - 1),
                            List.of(),
                            Tokenizer.tokenizeWord(glossWords.get(j - 1))
                    ));
                    i = step.previousI();
                    j = step.previousJ();
                }
            }
        }

        Collections.reverse(aligned);
        return aligned;
    }
}