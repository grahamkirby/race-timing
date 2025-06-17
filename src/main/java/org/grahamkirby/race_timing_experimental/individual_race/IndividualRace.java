package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing_experimental.common.RaceImpl;

import java.util.ArrayList;
import java.util.List;

public class IndividualRace implements SingleRaceType {

    RaceImpl single_race;
    List<RaceResult> results;

    public IndividualRace(RaceImpl single_race) {
        this.single_race = single_race;
    }

    @Override
    public synchronized List<RaceResult> calculateResults() {

        if (results == null) {
            results = new ArrayList<>();
        }

        return results;
    }

    @Override
    public void outputResults(List<RaceResult> results) {

    }
}
