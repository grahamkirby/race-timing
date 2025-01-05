package org.grahamkirby.race_timing.common;

/** Enumerates options for runner/team completion status for race or series. */
public enum CompletionStatus {

    /** Completed race or series. */
    COMPLETED,

    /** Did Not Start: appears in entry list but no finish(es) recorded. */
    DNS,

    /**
     * Did Not Finish: did not complete all legs (relay race) or sufficient component races (series race).
     */
    DNF
}
