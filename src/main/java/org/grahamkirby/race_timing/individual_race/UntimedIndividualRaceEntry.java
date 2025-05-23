package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.util.List;

class UntimedIndividualRaceEntry extends SingleRaceEntry {

    // Expected input format: "1", "John Smith", "Fife AC", "MS".
    private static final int BIB_NUMBER_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int CLUB_INDEX = 2;
    private static final int CATEGORY_INDEX = 3;

    UntimedIndividualRaceEntry(final List<String> elements, final Race race) {

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
}
