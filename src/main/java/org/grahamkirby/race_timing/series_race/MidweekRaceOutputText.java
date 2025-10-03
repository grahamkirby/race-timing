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
import org.grahamkirby.race_timing.common.CommonRaceResult;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinterText;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsOutput;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

public class MidweekRaceOutputText {

    private final Race race;
    MidweekRaceOutputText(final Race race) {
        this.race = race;
    }

    void printPrizes() throws IOException {

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "prizes", TEXT_FILE_SUFFIX);

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

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "processing_notes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(race.getNotes());
        }
    }

    private String getPrizesHeader() {

        final String header = race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race.getConfig().get(KEY_YEAR);
        return header + LINE_SEPARATOR + "=".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    /** Prints prizes, ordered by prize category groups. */
    private void printPrizes(final OutputStreamWriter writer) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).            // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(writer, category));         // Print prizes in this category.
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
        return header + LINE_SEPARATOR + "-".repeat(header.length())  + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    static class PrizeResultPrinter extends ResultPrinterText {

        PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = (MidweekRaceResult) r;

            writer.append(result.getPositionString() + ": " + result.getParticipantName() + " (" + ((Runner) result.getParticipant()).club + ") " + result.totalScore() + LINE_SEPARATOR);
        }
    }
}
