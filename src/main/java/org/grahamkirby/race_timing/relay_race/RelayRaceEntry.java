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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceEntry;

import java.util.Arrays;

public class RelayRaceEntry extends RaceEntry {

    public record Team(String name, Category category, String[] runners) {}

    public Team team;

    public RelayRaceEntry(final String[] elements, final Race race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        if (elements.length != ((RelayRace)race).number_of_legs + 3)
            throw new RuntimeException("illegal composition for team: " + elements[0]);

        bib_number = Integer.parseInt(elements[0]);
        try {
            final String name = elements[1];
            final Category category = race.lookupCategory(elements[2]);
            final String[] runners = Arrays.copyOfRange(elements, 3, elements.length);

            team = new Team(name, category, runners);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for team: " + bib_number);
        }
    }

    @Override
    public String toString() {
        return team.name;
    }
}
