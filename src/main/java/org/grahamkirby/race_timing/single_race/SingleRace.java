/*
 * Copyright 2024 Graham Kirby:
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

    public List<RaceEntry> entries;
    protected List<RawResult> raw_results;

    protected String dnf_string;

    public SingleRace(final Path config_file_path) throws IOException {
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

        entries = ((SingleRaceInput)input).loadEntries();
        raw_results = ((SingleRaceInput)input).loadRawResults();

        overall_results = ((SingleRaceInput)input).loadOverallResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<RawResult> getRawResults() {
        return raw_results;
    }

    protected void fillDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // DNF cases where there is no recorded leg result are captured by the
        // default value of DNF being true.

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                fillDNF(individual_dnf_string);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void fillDNF(String individual_dnf_string);
}
