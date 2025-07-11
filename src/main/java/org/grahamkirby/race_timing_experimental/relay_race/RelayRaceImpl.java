/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.relay_race;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RelayRaceImpl {

    public List<LegResult> getLegResults(int leg) {
        return null;
    }

    public List<String> getLegDetails(RelayRaceResult result) {
        return null;
    }

    public Map<Integer, Integer> countLegsFinishedPerTeam() {
        return null;
    }

    public List<Integer> getBibNumbersWithMissingTimes(Map<Integer, Integer> legsFinishedPerTeam) {
        return null;
    }

    public List<Duration> getTimesWithMissingBibNumbers() {
        return null;
    }

    /** Packages details of an individually recorded leg start (unusual). */
    public record IndividualStart(int bib_number, int leg_number, Duration start_time) {
    }


    public int getNumberOfLegs() {
        return 0;
    }

    /**
     * The number of legs in the relay race.
     * Value is read from configuration file using key KEY_NUMBER_OF_LEGS.
     */
    int number_of_legs;

    /**
     * For each leg, records whether there was a mass start.
     * Values are read from configuration file using key KEY_MASS_START_ELAPSED_TIMES.
     */
    List<Boolean> mass_start_legs;

    /**
     * For each leg, records whether it is a leg for paired runners.
     * Values are read from configuration file using key KEY_PAIRED_LEGS.
     */
    List<Boolean> paired_legs;

    /**
     * Times relative to start of leg 1 at which each mass start occurred.
     * For leg 2 onward, legs that didn't have a mass start are recorded with the time of the next actual
     * mass start. This allows e.g. for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
     *
     * Values are read from configuration file using key KEY_MASS_START_ELAPSED_TIMES.
     */
    List<Duration> start_times_for_mass_starts;

    /**
     * List of individually recorded starts (usually empty).
     * Values are read from configuration file using key KEY_INDIVIDUAL_LEG_STARTS.
     */
     List<IndividualStart> individual_starts;

    int getNumberOfRawResults() {
        return 0;
    }

    /**
     * Offset between actual race start time, and the time at which timing started.
     * Usually this is zero. A positive value indicates that the race started before timing started.
     *
     * Value is read from configuration file using key KEY_START_OFFSET.
     */
    Duration start_offset;
}
