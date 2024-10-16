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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.categories.Category;

import java.util.List;

public class RelayRaceEntry extends RaceEntry {

    private static final int BIB_NUMBER_INDEX = 0;
    private static final int TEAM_NAME_INDEX = 1;
    private static final int CATEGORY_INDEX = 2;
    private static final int FIRST_RUNNER_NAME_INDEX = 3;

    public record Team(String name, Category category, List<String> runners) {}

    public Team team;

    public RelayRaceEntry(final List<String> elements, final Race race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        if (elements.size() != FIRST_RUNNER_NAME_INDEX + ((RelayRace)race).number_of_legs)
            throw new RuntimeException("illegal composition for team: " + elements.get(BIB_NUMBER_INDEX));

        bib_number = Integer.parseInt(elements.get(BIB_NUMBER_INDEX));
        try {
            final String name = elements.get(TEAM_NAME_INDEX);
            final Category category = race.lookupCategory(elements.get(CATEGORY_INDEX));

            final List<String> runners = elements.subList(FIRST_RUNNER_NAME_INDEX, elements.size()).stream().map(s -> race.normalisation.cleanName(s)).toList();

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
