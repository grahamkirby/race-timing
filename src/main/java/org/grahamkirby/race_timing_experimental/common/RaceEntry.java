/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.util.List;

public class RaceEntry {

    // Expected input format: "1", "John Smith", "Fife AC", "MS".
    private static final int BIB_NUMBER_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int CLUB_INDEX = 2;
    public static final int CATEGORY_INDEX = 3;

    public Participant participant;
    public int bib_number;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "OverlyBroadCatchBlock", "IfCanBeAssertion"})
    public RaceEntry(final List<String> elements, final Race race) {

        Normalisation normalisation = race.getNormalisation();
        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            bib_number = Integer.parseInt(mapped_elements.get(BIB_NUMBER_INDEX));

            final String name = normalisation.cleanRunnerName(mapped_elements.get(NAME_INDEX));
            final String club = normalisation.cleanClubOrTeamName(mapped_elements.get(CLUB_INDEX));

            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            final EntryCategory category = race.getCategoryDetails().lookupEntryCategory(category_name);

            participant = new Runner(name, club, category);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    public RaceEntry(final Participant participant, final int bib_number, final Race race) {

        this.participant = participant;
        this.bib_number = bib_number;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public boolean equals(Object obj) {
        return obj instanceof RaceEntry entry && entry.bib_number == bib_number && entry.participant.equals(participant);
    }

    @Override
    public String toString() {
        return participant.toString();
    }
}
