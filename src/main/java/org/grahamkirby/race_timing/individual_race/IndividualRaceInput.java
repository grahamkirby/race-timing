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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.util.List;

import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

class IndividualRaceInput extends SingleRaceInput {

    IndividualRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected RaceEntry makeRaceEntry(final List<String> elements) {

        return new IndividualRaceEntry(elements, race);
    }

    @Override
    public void validateInputFiles() {

        super.validateInputFiles();
        checkConfig();

        checkResultsContainValidBibNumbers();
    }

    private void checkConfig() {

        final String dnf_string = race.getOptionalProperty(KEY_DNF_FINISHERS);
        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(",")) {
                try {
                    Integer.parseInt(individual_dnf_string);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException(STR."invalid entry '\{individual_dnf_string}' for key '\{KEY_DNF_FINISHERS}' in file '\{race.config_file_path.getFileName()}'", e);
                }
            }
    }
}
