/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.single_race;


import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_DNF_FINISHERS;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_MEDIAN_TIME;

public abstract class SingleRace extends Race {

    // Configuration file keys.
//    public static final String KEY_RESULTS_PATH = "RESULTS_PATH";
//    public static final String KEY_DNF_FINISHERS = "DNF_FINISHERS";
//    private static final String KEY_MEDIAN_TIME = "MEDIAN_TIME";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public String dnf_string;
    private String median_time_string;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected SingleRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void recordDNF(String dnf_specification);

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
        dnf_string = getOptionalProperty(KEY_DNF_FINISHERS);
        median_time_string = getOptionalProperty(KEY_MEDIAN_TIME);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Gets the median finish time for the race. */
    public Duration getMedianTime() {

        // The median time may be recorded explicitly if not all results are recorded.
        if (median_time_string != null) return parseTime(median_time_string);

        final List<RaceResult> results = getOverallResults();

        if (results.size() % 2 == 0) {

            final SingleRaceResult median_result1 = (SingleRaceResult) results.get(results.size() / 2 - 1);
            final SingleRaceResult median_result2 = (SingleRaceResult) results.get(results.size() / 2);

            return median_result1.finish_time.plus(median_result2.finish_time).dividedBy(2);

        } else {
            final SingleRaceResult median_result = (SingleRaceResult) results.get(results.size() / 2);
            return median_result.finish_time;
        }
    }

    /** Gets the finish time for the given runner. */
    public Duration getRunnerTime(final Participant participant) {

        for (final RaceResult result : getOverallResults()) {

            final SingleRaceResult individual_result = (SingleRaceResult) result;
            if (individual_result.getParticipant().equals(participant))
                return individual_result.duration();
        }

        return null;
    }

    protected void recordDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                recordDNF(individual_dnf_string);
    }
}
