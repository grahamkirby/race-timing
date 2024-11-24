package org.grahamkirby.race_timing.common;

public enum CompletionStatus {

    // TODO enum for completion status - normal, DNS, DNF, recorded but DNF

    COMPLETED, DNS, DNF, RECORDED;

    // TODO tidy logic around number of races completed, possible series completion etc
    // TODO rationalise with IndividualRace with respect to treatment of non-starts or non finishers.
    // TODO unify compareCompletionSoFar and compareCompletion into compareCanComplete

    int numberOfRacesCompleted() {
        return 0;
    }

    boolean completedSeries() {
        return false;
    }

    boolean canCompleteSeries() {
        return false;
    }
}
