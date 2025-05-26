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
import org.grahamkirby.race_timing.common.RaceInput;

import java.util.List;

import static org.grahamkirby.race_timing.single_race.SingleRace.*;

@SuppressWarnings({"IncorrectFormatting", "NonBooleanMethodNameMayNotStartWithQuestion"})
public abstract class SingleRaceInput extends RaceInput {

    protected SingleRaceInput(final Race race) {

        super(race);
        readProperties();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract SingleRaceEntry makeRaceEntry(final List<String> elements);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        raw_results_path = race.getOptionalProperty(KEY_RAW_RESULTS_PATH);
        overall_results_path = race.getOptionalProperty(KEY_RESULTS_PATH);
    }

    public void validateInputFiles() {

        checkEntries();
    }

    protected abstract void checkEntries();
}
