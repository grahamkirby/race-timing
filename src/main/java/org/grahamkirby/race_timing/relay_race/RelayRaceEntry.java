/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.relay_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.util.List;

public class RelayRaceEntry extends SingleRaceEntry {

    private static final int BIB_NUMBER_INDEX = 0;
    private static final int TEAM_NAME_INDEX = 1;
    private static final int CATEGORY_INDEX = 2;
    private static final int FIRST_RUNNER_NAME_INDEX = 3;

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    RelayRaceEntry(final List<String> elements, final Race race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        if (elements.size() != FIRST_RUNNER_NAME_INDEX + ((RelayRace) race).getNumberOfLegs())
            throw new RuntimeException(String.join(" ", elements));

        bib_number = Integer.parseInt(elements.get(BIB_NUMBER_INDEX));
        try {
            final String name = elements.get(TEAM_NAME_INDEX);
            final EntryCategory category = race.lookupEntryCategory(elements.get(CATEGORY_INDEX));

            final List<String> runners = elements.subList(FIRST_RUNNER_NAME_INDEX, elements.size()).stream().map(s -> race.normalisation.cleanRunnerName(s)).toList();

            participant = new Team(name, category, runners);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    @Override
    public String toString() {
        return participant.name;
    }
}
