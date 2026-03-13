package org.titiplex.align;

record AlignmentStep(int previousI, int previousJ, StepType type) {
    enum StepType {
        MATCH,
        DELETE_GLOSS,
        INSERT_GLOSS
    }
}
