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
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.util.List;
import java.util.Objects;

public class TimedIndividualRaceEntry extends TimedRaceEntry {

    // Expected input format: "1", "John Smith", "Fife AC", "MS".
    private static final int BIB_NUMBER_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int CLUB_INDEX = 2;
    private static final int CATEGORY_INDEX = 3;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "OverlyBroadCatchBlock", "IfCanBeAssertion"})
    TimedIndividualRaceEntry(final List<String> elements, final Race race) {

        final List<String> mapped_elements = race.normalisation.mapRaceEntryElements(elements);

        try {
            bib_number = Integer.parseInt(mapped_elements.get(BIB_NUMBER_INDEX));

            final String name = race.normalisation.cleanRunnerName(mapped_elements.get(NAME_INDEX));
            final String club = race.normalisation.cleanClubOrTeamName(mapped_elements.get(CLUB_INDEX));

            final String category_name = race.normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            final EntryCategory category = category_name.isEmpty() ? null : race.lookupEntryCategory(category_name);

            participant = new Runner(name, club, category);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return STR."\{participant.name}, \{((Runner)participant).club}";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final TimedIndividualRaceEntry other_entry &&
            ((Runner)participant).equals(((Runner)other_entry.participant));
    }

    @Override
    public int hashCode() {
        return Objects.hash(participant.name, ((Runner)participant).club);
    }
}
