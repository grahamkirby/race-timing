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

import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.util.List;

import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

class TimedIndividualRaceInput extends TimedRaceInput {

    TimedIndividualRaceInput(final TimedIndividualRace race) {
        super(race);
    }

    @Override
    protected SingleRaceEntry makeRaceEntry(final List<String> elements) {

        return new IndividualRaceEntry(elements, race);
    }

    @Override
    protected void validateConfig() {

        super.validateConfig();
        validateDNFRecords();
    }

    private void validateDNFRecords() {

        final String dnf_string = ((SingleRace) race).dnf_string;

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String bib_number : dnf_string.split(",")) {
                try {
                    Integer.parseInt(bib_number);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException(STR."invalid entry '\{bib_number}' for key '\{KEY_DNF_FINISHERS}' in file '\{race.config_file_path.getFileName()}'", e);
                }
            }
    }
}
