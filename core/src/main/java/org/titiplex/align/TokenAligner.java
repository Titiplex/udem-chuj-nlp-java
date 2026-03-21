package org.titiplex.align;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TokenAligner {
    private final int gapCostChuj;
    private final int gapCostGloss;
    private final AlignmentTieBreakStrategy tieBreakStrategy;

    public TokenAligner() {
        this(2, 2, AlignmentTieBreakStrategy.GLOSS_GAP_FIRST);
    }

    public TokenAligner(int gapCostChuj, int gapCostGloss) {
        this(gapCostChuj, gapCostGloss, AlignmentTieBreakStrategy.GLOSS_GAP_FIRST);
    }

    public TokenAligner(int gapCostChuj, int gapCostGloss, AlignmentTieBreakStrategy tieBreakStrategy) {
        this.gapCostChuj = gapCostChuj;
        this.gapCostGloss = gapCostGloss;
        this.tieBreakStrategy = tieBreakStrategy;
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

                dp[i][j] = Math.min(matchCost, Math.min(deleteGlossCost, insertGlossCost));
                back[i][j] = chooseBestStep(i, j, matchCost, deleteGlossCost, insertGlossCost);
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

    private AlignmentStep chooseBestStep(int i, int j, int matchCost, int deleteGlossCost, int insertGlossCost) {
        int best = Math.min(matchCost, Math.min(deleteGlossCost, insertGlossCost));

        List<AlignmentStep.StepType> priority = switch (tieBreakStrategy) {
            case MATCH_FIRST -> List.of(
                    AlignmentStep.StepType.MATCH,
                    AlignmentStep.StepType.INSERT_GLOSS,
                    AlignmentStep.StepType.DELETE_GLOSS
            );
            case GLOSS_GAP_FIRST -> List.of(
                    AlignmentStep.StepType.INSERT_GLOSS,
                    AlignmentStep.StepType.MATCH,
                    AlignmentStep.StepType.DELETE_GLOSS
            );
            case CHUJ_GAP_FIRST -> List.of(
                    AlignmentStep.StepType.DELETE_GLOSS,
                    AlignmentStep.StepType.MATCH,
                    AlignmentStep.StepType.INSERT_GLOSS
            );
        };

        for (AlignmentStep.StepType type : priority) {
            switch (type) {
                case MATCH -> {
                    if (matchCost == best) {
                        return new AlignmentStep(i - 1, j - 1, AlignmentStep.StepType.MATCH);
                    }
                }
                case DELETE_GLOSS -> {
                    if (deleteGlossCost == best) {
                        return new AlignmentStep(i - 1, j, AlignmentStep.StepType.DELETE_GLOSS);
                    }
                }
                case INSERT_GLOSS -> {
                    if (insertGlossCost == best) {
                        return new AlignmentStep(i, j - 1, AlignmentStep.StepType.INSERT_GLOSS);
                    }
                }
            }
        }

        throw new IllegalStateException("No valid alignment step found.");
    }
}