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
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.Category;

public class IndividualRaceEntry extends RaceEntry {

    public final Runner runner;

    public IndividualRaceEntry(final String[] elements, final Race race) {

        // Expected format: "1", "John Smith", "Fife AC", "MS".

        if (elements.length != 4)
            throw new RuntimeException("illegal composition for runner: " + elements[0]);

        try {
            bib_number = Integer.parseInt(elements[0]);

            final String name = race.normalisation.cleanName(elements[1]);
            final String club = race.normalisation.normaliseClubName(race.normalisation.cleanName(elements[2]));
            final Category category = race.lookupCategory(elements[3]);

            runner = new Runner(name, club, category);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for runner: " + bib_number);
        }
    }

    @Override
    public String toString() {
        return runner.name + ", " + runner.club;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IndividualRaceEntry other_entry &&
                runner.name.equals(other_entry.runner.name) &&
                runner.club.equals(other_entry.runner.club);
    }
}
