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

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing_experimental.common.RaceData;
import org.grahamkirby.race_timing_experimental.common.RaceEntry;

import java.util.List;
import java.util.Map;

public class RelayRaceDataImpl implements RaceData {

    private final List<RawResult> raw_results;
    private final List<RaceEntry> entries;
    public final Map<RawResult, Integer> explicitly_recorded_leg_numbers;
    public final int number_of_raw_results;

    public RelayRaceDataImpl(List<RaceEntry> entries, List<RawResult> raw_results, Map<RawResult, Integer> explicitly_recorded_leg_numbers, int number_of_raw_results) {

        this.raw_results = raw_results;
        this.entries = entries;
        this.explicitly_recorded_leg_numbers = explicitly_recorded_leg_numbers;
        this.number_of_raw_results = number_of_raw_results;
    }

    @Override
    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public List<RaceEntry> getEntries() {
        return entries;
    }
}
