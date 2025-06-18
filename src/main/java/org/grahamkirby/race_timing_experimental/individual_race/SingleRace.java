package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceImpl;

import java.io.IOException;
import java.util.List;

public class SingleRace {

    // Configuration file keys.
    public static final String KEY_RESULTS_PATH = "RESULTS_PATH";
    public static final String KEY_DNF_FINISHERS = "DNF_FINISHERS";
    private static final String KEY_MEDIAN_TIME = "MEDIAN_TIME";
    public static final String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    public static final String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    Race race;
    SingleRaceType single_race_impl;
    RaceInput race_input;

    protected String entries_path, raw_results_path;
    public List<SingleRaceEntry> entries;
    protected List<RawResult> raw_results;

    public Race getRace() {return race;}

    public String dnf_string;

    public SingleRace(Race race) {
        this.race = race;
    }

    public void setRaceType(SingleRaceType single_race_impl) {
        this.single_race_impl = single_race_impl;
    }

    public void setRaceInput(RaceInput race_input) {
        this.race_input = race_input;
    }

    public void processProperties() {

        // Specifies all the bib numbers for runners who did have a finish
        // time recorded but were declared DNF.
        dnf_string = race.getProperties().getProperty(KEY_DNF_FINISHERS);
        entries_path = race.getProperties().getProperty(KEY_ENTRIES_PATH);
        raw_results_path = race.getProperties().getProperty(KEY_RAW_RESULTS_PATH);
    }

    public synchronized List<RaceResult> calculateResults() {

        return single_race_impl.calculateResults();
    }

    public void outputResults(List<RaceResult> results) {

        single_race_impl.outputResults(results);
    }

    public void configureInputData() throws IOException {

        entries = race_input.loadEntries(race.getPath(entries_path));
        raw_results = race_input.loadRawResults(race.getPath(raw_results_path));
    }
}
