package org.grahamkirby.race_timing.common;

public enum CompletionStatus {

    /**
     * Completed race or series.
     */
    COMPLETED,

    /**
     * Did Not Start: runner appears in entry but no finish(es) recorded.
     */
    DNS,

    /**
     * Did Not Finish: did not complete all legs or sufficient component races.
     */
    DNF
}
