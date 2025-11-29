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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.List;

public class RaceEntry {

    // Expected input format: "1", "John Smith", "Fife AC", "MS".
    public static final int BIB_NUMBER_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int CLUB_INDEX = 2;
    public static final int CATEGORY_INDEX = 3;

    protected final Participant participant;
    protected final int bib_number;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({"OverlyBroadCatchBlock", "IfCanBeAssertion"})
    public RaceEntry(final List<String> elements, final SingleRaceInternal race) {

        final Normalisation normalisation = race.getNormalisation();

        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            bib_number = Integer.parseInt(mapped_elements.get(BIB_NUMBER_INDEX));

            final String name = normalisation.cleanRunnerName(mapped_elements.get(NAME_INDEX));
            final String club = normalisation.cleanClubOrTeamName(mapped_elements.get(CLUB_INDEX));

            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            final EntryCategory category = category_name.isEmpty() ? null : race.getCategoriesProcessor().getEntryCategory(category_name);

            participant = new Runner(name, club, category);

        } catch (final RuntimeException e) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    public RaceEntry(final Participant participant, final int bib_number) {

        this.participant = participant;
        this.bib_number = bib_number;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public int getBibNumber() {
        return bib_number;
    }

    public Participant getParticipant() {
        return participant;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final RaceEntry entry && entry.bib_number == bib_number && entry.participant.equals(participant);
    }

    @Override
    public String toString() {
        return participant.toString();
    }
}
