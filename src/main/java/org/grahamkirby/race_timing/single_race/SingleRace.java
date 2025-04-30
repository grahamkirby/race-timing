/*
 * Copyright 2025 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.single_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.RawResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class SingleRace extends Race {

    // Configuration file keys.
    public static final String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    public static final String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    static final String KEY_RESULTS_PATH = "RESULTS_PATH";
    public static final String KEY_DNF_FINISHERS = "DNF_FINISHERS";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<RaceEntry> entries;
    protected List<RawResult> raw_results;

    private String dnf_string;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected SingleRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean areEqualPositionsAllowed() {

        // No dead heats for overall results, since an ordering is imposed at the finish.
        return false;
    }

    @Override
    protected void readProperties() throws IOException {

        // Specifies all the bib numbers for runners who did have a finish
        // time recorded but were declared DNF.
        dnf_string = getProperty(KEY_DNF_FINISHERS, "");
    }

    @Override
    protected void configureInputData() throws IOException {

        input.validateInputs();

        final SingleRaceInput single_race_input = (SingleRaceInput) input;

        entries = single_race_input.loadEntries();

        // Only one of raw_results and overall_results will be fully initialised at this point,
        // depending on whether raw results are available, or just overall results (perhaps for
        // an externally organised race included in a race series).
        // The other list will be initialised as an empty list.
        raw_results = single_race_input.loadRawResults();
        overall_results = single_race_input.loadOverallResults();

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<RawResult> getRawResults() {
        return raw_results;
    }

    protected void recordDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

        try {
            if (dnf_string != null && !dnf_string.isBlank())
                for (final String individual_dnf_string : dnf_string.split(","))
                    recordDNF(individual_dnf_string);
        }
        catch (RuntimeException e) {
            throw new RuntimeException(STR."invalid entry for key '\{KEY_DNF_FINISHERS}' in file '\{config_file_path.getFileName()}': \{e.getMessage()}");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void recordDNF(String dnf_specification);
}
