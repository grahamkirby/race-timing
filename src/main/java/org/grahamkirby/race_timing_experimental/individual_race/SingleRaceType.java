package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.RaceResult;

import java.util.List;

public interface SingleRaceType {

    List<RaceResult> calculateResults();

    void outputResults(List<RaceResult> results);
}
