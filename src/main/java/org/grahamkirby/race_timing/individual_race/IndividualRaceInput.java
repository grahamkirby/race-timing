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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.util.List;

public class IndividualRaceInput extends SingleRaceInput {

    public IndividualRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected RaceEntry makeRaceEntry(String[] elements) {
        return new IndividualRaceEntry(elements, race);
    }

    @Override
    public List<RawResult> loadRawResults() throws IOException {

        return loadRawResults(raw_results_path);
    }

    @Override
    protected void checkDuplicateEntry(final List<RaceEntry> entries, final RaceEntry new_entry) {

        for (final RaceEntry entry : entries)
            if (((IndividualRaceEntry)entry).runner.name.equals(((IndividualRaceEntry) new_entry).runner.name) && ((IndividualRaceEntry)entry).runner.club.equals(((IndividualRaceEntry) new_entry).runner.club))
                throw new RuntimeException("duplicate entry: " + new_entry);
    }
}
