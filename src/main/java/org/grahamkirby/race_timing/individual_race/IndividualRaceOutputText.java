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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

/** Base class for plaintext output. */
@SuppressWarnings("preview")
public class IndividualRaceOutputText {

    private final Race race;

    protected IndividualRaceOutputText(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Prints race prizes. Used for HTML and text output.
     *
     * @throws IOException if an I/O error occurs.
     */
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

    /** Prints prizes, ordered by prize category groups. */
    private void printPrizes(final OutputStreamWriter writer) throws IOException {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).            // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(writer, category));         // Print prizes in this category.

        printTeamPrizes(writer);
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

    private void printTeamPrizes(final OutputStreamWriter writer) throws IOException {

        final List<String> team_prizes = ((IndividualRaceImpl) race.getSpecific()).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            writer.append("Team Prizes\n");
            writer.append("-----------\n\n");

            for (final String team_prize : team_prizes) {
                writer.append(team_prize);
                writer.append(LINE_SEPARATOR);
            }
        }
    }

    private String getPrizesHeader() {

        final String header = race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race.getConfig().get(KEY_YEAR);
        return header + LINE_SEPARATOR + "=".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class PrizeResultPrinter extends ResultPrinterText {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;

            writer.append(result.getPositionString() + ": " + result.getParticipantName() + " (" + ((Runner) result.getParticipant()).club + ") " + renderDuration(result, DNF_STRING) + LINE_SEPARATOR);
        }
    }
}
