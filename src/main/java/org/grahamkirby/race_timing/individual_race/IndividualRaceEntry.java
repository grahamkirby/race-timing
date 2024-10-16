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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndividualRaceEntry extends RaceEntry {

    // Expected input format: "1", "John Smith", "Fife AC", "MS".
    private static final int EXPECTED_NUMBER_OF_ENTRY_ELEMENTS = 4;

    public final Runner runner;

    public IndividualRaceEntry(final List<String> elements, final Race race) {

        final List<String> mapped_elements = mapElements(elements, race.entry_column_map_string);

        if (mapped_elements.size() != EXPECTED_NUMBER_OF_ENTRY_ELEMENTS)
            throw new RuntimeException("illegal composition for runner: " + mapped_elements.get(0));

        try {
            bib_number = Integer.parseInt(mapped_elements.get(0));

            final String name = race.normalisation.cleanName(mapped_elements.get(1));
            final String club = race.normalisation.normaliseClubName(race.normalisation.cleanName(mapped_elements.get(2)));
            final Category category = race.lookupCategory(race.mapCategory(mapped_elements.get(3)));

            runner = new Runner(name, club, category);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for runner: " + bib_number);
        }
    }

    private List<String> mapElements(final List<String> elements, final String entry_column_map_string) {

        // Expected format of map string: "1 3-2 4 5",
        // meaning elements 2 and 3 should be swapped and concatenated with a space to give compound element.

        final List<String> map_elements = Arrays.stream(entry_column_map_string.split(" ")).toList();
        final List<String> result = new ArrayList<>();

        for (int i = 0; i < map_elements.size(); i++)
            result.add(getMappedElement(elements, map_elements.get(i)));
        
        return result;
    }

    private static String getMappedElement(final List<String> elements, final String element_combination_map) {
        
        final StringBuilder element_builder = new StringBuilder();

        for (final String column_number_as_string : element_combination_map.split("-")) {

            final int index = Integer.parseInt(column_number_as_string) - 1;

            if (!element_builder.isEmpty()) element_builder.append(" ");
            element_builder.append(elements.get(index));
        }
        
        return element_builder.toString();
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
