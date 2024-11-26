package org.grahamkirby.race_timing.common;

public enum CompletionStatus {

    COMPLETED,      // Completed race or series.
    DNS,            // Did Not Start: runner appears in entry but no finish(es) recorded.
    DNF             // Did Not Finish: did not complete all legs or sufficient component races.
}
