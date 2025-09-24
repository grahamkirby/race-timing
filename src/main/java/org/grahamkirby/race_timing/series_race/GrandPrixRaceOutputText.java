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


import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinterText;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

public class GrandPrixRaceOutputText {

    private final Race race;
    GrandPrixRaceOutputText(final Race race) {
        this.race = race;
    }

    void printPrizes() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(getOutputFilePath(race_name, "prizes", year), STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeader());
            printPrizes(writer);
        }
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    void printNotes() throws IOException {

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(getOutputFilePath(race_name, "processing_notes", year), STANDARD_FILE_OPEN_OPTIONS))) {
            writer.append(race.getNotes());
        }
    }

    private String getPrizesHeader() {

        final String header = race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race.getConfig().get(KEY_YEAR);
        return header + LINE_SEPARATOR + "=".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    /**
     * Constructs a path for a file in the project output directory with name constructed from the given components.
     */
    private Path getOutputFilePath(final String race_name, final String output_type, final String year) {

        return race.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + TEXT_FILE_SUFFIX);
    }

    /** Prints prizes, ordered by prize category groups. */
    private void printPrizes(final OutputStreamWriter writer) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(writer, category));       // Print prizes in this category.
    }

    /** Prints prizes within a given category. */
    private void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            writer.append(getPrizeCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            new PrizeResultPrinter(race, writer).print(category_prize_winners);

            writer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPrizeCategoryHeader(final PrizeCategory category) {

        final String header = "Category: " + category.getLongName();
        return header + LINE_SEPARATOR + "-".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    static class PrizeResultPrinter extends ResultPrinterText {

        PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = (GrandPrixRaceResult) r;

            writer.append(result.getPositionString() + ": " + result.runner.name + " (" + result.runner.club + ") " + result.totalScore() + LINE_SEPARATOR);
        }
    }
}
