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
package org.grahamkirby.race_timing.series_race;


import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.relay_race.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;
import static org.grahamkirby.race_timing.common.RaceOutput.getOutputStream;

public class SeriesRaceOutputText {

    private final Race race;
    SeriesRaceOutputText(final Race race) {
        this.race = race;
    }

    void printPrizesCSV() throws IOException {

        printPrizesCSV(race);
    }

    public static void printPrizesCSV(final Race race) throws IOException {

        final OutputStream stream = getOutputStream(race, "prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeader(race));
            printPrizesText(writer, race);
        }
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    void printNotes() throws IOException {

        printNotes(race);
    }

    public static void printNotes(final Race race) throws IOException {

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);

        final OutputStream stream = getOutputStream(race, "processing_notes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(race.getNotes());
        }
    }

    public static String getPrizesHeader(final Race race) {

        final String header = race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race.getConfig().get(KEY_YEAR);
        return header + LINE_SEPARATOR + "=".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    public static void printPrizesText(final OutputStreamWriter writer, final Race race) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> RelayRaceOutput.printPrizesText(writer, category, race));       // Print prizes in this category.
    }
}
