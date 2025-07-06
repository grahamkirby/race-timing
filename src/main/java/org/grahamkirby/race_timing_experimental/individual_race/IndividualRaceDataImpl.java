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

import java.util.List;

public class IndividualRaceDataImpl implements IndividualRaceData {

    private final List<RawResult> raw_results;
    private final List<IndividualRaceEntry> entries;

    public IndividualRaceDataImpl(List<RawResult> raw_results, List<IndividualRaceEntry> entries) {

        this.raw_results = raw_results;
        this.entries = entries;
    }

    @Override
    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public List<IndividualRaceEntry> getEntries() {
        return entries;
    }
}
