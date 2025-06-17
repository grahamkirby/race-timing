package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing_experimental.individual_race.RaceInput;
import org.grahamkirby.race_timing_experimental.individual_race.SingleRaceType;

import java.io.IOException;
import java.util.List;

public interface RaceImpl {

    Race getRace();

    void setRaceInput(RaceInput race_input);

    void processProperties();

    List<RaceResult> calculateResults();

    void outputResults(List<RaceResult> results);

    void configureInputData() throws IOException;

    void setRaceType(SingleRaceType individualRace);
}
