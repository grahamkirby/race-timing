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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing_experimental.common.RaceData;

import java.util.List;

public class IndividualRaceData implements RaceData {

    public IndividualRaceData(List<RawResult> rawResults, List<IndividualRaceEntry> singleRaceEntries) {
    }

    @Override
    public List<RawResult> getRawResults() {
        return List.of();
    }

    @Override
    public List<IndividualRaceEntry> getEntries() {
        return List.of();
    }
}
