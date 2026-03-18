package org.titiplex.align;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MorphemeAligner {
    private final int gapCost;

    public MorphemeAligner() {
        this(2);
    }

    public MorphemeAligner(int gapCost) {
        this.gapCost = gapCost;
    }

    public List<MorphemeAlignment> align(List<String> chujSegments, List<String> glossSegments) {
        int n = chujSegments.size();
        int m = glossSegments.size();
        int inf = 1_000_000;

        int[][] dp = new int[n + 1][m + 1];
        AlignmentStep[][] back = new AlignmentStep[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dp[i][j] = inf;
            }
        }
        dp[0][0] = 0;

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                if (dp[i][j] >= inf) {
                    continue;
                }

                if (i < n && j < m) {
                    int match = dp[i][j] + Math.abs(chujSegments.get(i).length() - glossSegments.get(j).length());
                    if (match < dp[i + 1][j + 1]) {
                        dp[i + 1][j + 1] = match;
                        back[i + 1][j + 1] = new AlignmentStep(i, j, AlignmentStep.StepType.MATCH);
                    }
                }

                if (i < n) {
                    int deleteGloss = dp[i][j] + gapCost;
                    if (deleteGloss < dp[i + 1][j]) {
                        dp[i + 1][j] = deleteGloss;
                        back[i + 1][j] = new AlignmentStep(i, j, AlignmentStep.StepType.DELETE_GLOSS);
                    }
                }

                if (j < m) {
                    int insertGloss = dp[i][j] + gapCost;
                    if (insertGloss < dp[i][j + 1]) {
                        dp[i][j + 1] = insertGloss;
                        back[i][j + 1] = new AlignmentStep(i, j, AlignmentStep.StepType.INSERT_GLOSS);
                    }
                }
            }
        }

        List<MorphemeAlignment> result = new ArrayList<>();
        int i = n;
        int j = m;

        while (i > 0 || j > 0) {
            AlignmentStep step = back[i][j];
            if (step == null) {
                break;
            }

            switch (step.type()) {
                case MATCH -> result.add(new MorphemeAlignment(i - 1, j - 1));
                case DELETE_GLOSS -> result.add(new MorphemeAlignment(i - 1, null));
                case INSERT_GLOSS -> result.add(new MorphemeAlignment(null, j - 1));
            }

            i = step.previousI();
            j = step.previousJ();
        }

        Collections.reverse(result);
        return result;
    }
}
