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

import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.Runner;

import java.util.HashMap;
import java.util.Map;

public class IndividualRaceEntry extends RaceEntry {

    private static final Map<String, String> NORMALISED_CLUB_NAMES = new HashMap<>();

    static {
        NORMALISED_CLUB_NAMES.put("", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Unattached", "Unatt.");
        NORMALISED_CLUB_NAMES.put("U/A", "Unatt.");
        NORMALISED_CLUB_NAMES.put("None", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Fife Athletic Club", "Fife AC");
        NORMALISED_CLUB_NAMES.put("Dundee HH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas Running Club", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Haddies", "Anster Haddies");
        NORMALISED_CLUB_NAMES.put("Dundee Hawkhill", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("DRR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Perth RR", "Perth Road Runners");
        NORMALISED_CLUB_NAMES.put("Kinross RR", "Kinross Road Runners");
        NORMALISED_CLUB_NAMES.put("Falkland TR", "Falkland Trail Runners");
        NORMALISED_CLUB_NAMES.put("PH Racing Club", "PH Racing");
        NORMALISED_CLUB_NAMES.put("DHH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Carnegie H", "Carnegie Harriers");
        NORMALISED_CLUB_NAMES.put("Dundee RR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Recreational Running", "Recreational Runners");
    }

    public final Runner runner;

    public IndividualRaceEntry(final String[] elements, final Race race) {

        // Expected format: "1" "John Smith"	"Fife AC"	"MS"

        if (elements.length != 4)
            throw new RuntimeException("illegal composition for runner: " + elements[0]);

        try {

            bib_number = Integer.parseInt(elements[0]);

            final String name = cleanName(elements[1]);
            final String club = normaliseClubName(cleanName(elements[2]));
            final Category category = race.lookupCategory(elements[3]);

            runner = new Runner(name, club, category);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("illegal category for runner: " + bib_number);
        }
    }

    private String cleanName(String name) {

        while (name.contains("  ")) name = name.replaceAll(" {2}", " ");
        return name.strip();
    }

    private static String normaliseClubName(final String club) {

        return NORMALISED_CLUB_NAMES.getOrDefault(club, club);
    }

    @Override
    public String toString() {
        return runner.name + ", " + runner.club;
    }
}
