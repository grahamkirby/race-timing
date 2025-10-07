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


import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.IndividualRaceOutputText;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsOutput;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputText;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

/** Base class for plaintext output. */
@SuppressWarnings("preview")
public class RelayRaceOutputText {

    private final Race race;

    protected RelayRaceOutputText(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Prints race prizes. Used for HTML and text output.
     *
     * @throws IOException if an I/O error occurs.
     */
    void printPrizes() throws IOException {
        SeriesRaceOutputText.printPrizes( race);
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    void printNotes() throws IOException {

        SeriesRaceOutputText.printNotes(race);
    }

    public static void printPrizes(final OutputStreamWriter writer, final PrizeCategory category, final Race race) {

        try {
            writer.append(getPrizeCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            new IndividualRaceOutputText.PrizeResultPrinter(race, writer).print(category_prize_winners);

            writer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getPrizeCategoryHeader(final PrizeCategory category) {

        final String header = "Category: " + category.getLongName();
        return header + LINE_SEPARATOR + "-".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printCollatedResults() throws IOException {

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "times_collated", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final Map<Integer, Integer> legs_finished_per_team = ((RelayRaceImpl)race.getSpecific()).countLegsFinishedPerTeam();

            printResults(writer, legs_finished_per_team);

            final List<Integer> bib_numbers_with_missing_times = ((RelayRaceImpl) race.getSpecific()).getBibNumbersWithMissingTimes(legs_finished_per_team);
            final List<Duration> times_with_missing_bib_numbers = ((RelayRaceImpl) race.getSpecific()).getTimesWithMissingBibNumbers();

            final boolean discrepancies_exist = !bib_numbers_with_missing_times.isEmpty() || !times_with_missing_bib_numbers.isEmpty();

            if (discrepancies_exist)
                race.appendToNotes("""
                
                Discrepancies:
                -------------
                """);

            printBibNumbersWithMissingTimes(bib_numbers_with_missing_times);
            printTimesWithMissingBibNumbers(times_with_missing_bib_numbers);

            if (discrepancies_exist)
                race.appendToNotes("""
                
                
                """);
        }
    }

    private void printResults(final OutputStreamWriter writer, final Map<Integer, Integer> legs_finished_per_team) throws IOException {

        for (final RawResult result : race.getRaceData().getRawResults()) {

            final int legs_already_finished = legs_finished_per_team.get(result.getBibNumber()) - 1;
            printResult(writer, result, legs_already_finished);
        }
    }

    private void printResult(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {

        printBibNumberAndTime(writer, raw_result);
        printLegNumber(writer, raw_result, legs_already_finished);
        printComment(writer, raw_result);
    }

    private static void printBibNumberAndTime(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final int bib_number = raw_result.getBibNumber();

        writer.append(bib_number != UNKNOWN_BIB_NUMBER ? String.valueOf(bib_number) : "?").
            append("\t").
            append(raw_result.getRecordedFinishTime() != null ? renderDuration(raw_result.getRecordedFinishTime()) : "?");
    }

    private void printLegNumber(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataImpl)race.getRaceData()).explicitly_recorded_leg_numbers;

        if (explicitly_recorded_leg_numbers.containsKey(raw_result)) {

            final int leg_number = explicitly_recorded_leg_numbers.get(raw_result);

            writer.append("\t").append(String.valueOf(leg_number));

            if (legs_already_finished >= leg_number)
                raw_result.appendComment("Leg " + leg_number + " finisher was runner " + (legs_already_finished + 1) + " to finish for team.");
        }
    }

    private void printComment(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataImpl) race.getRaceData()).explicitly_recorded_leg_numbers;

        if (!raw_result.getComment().isEmpty()) {

            if (!explicitly_recorded_leg_numbers.containsKey(raw_result)) writer.append("\t");
            writer.append("\t").append(COMMENT_SYMBOL).append(" ").append(raw_result.getComment());
        }

        writer.append(LINE_SEPARATOR);
    }

    @SuppressWarnings("IncorrectFormatting")
    private void printBibNumbersWithMissingTimes(final List<Integer> bib_numbers_with_missing_times) {

        if (!bib_numbers_with_missing_times.isEmpty()) {

            race.appendToNotes("""
                
                Bib numbers with missing times:\s""");

            race.appendToNotes(
                bib_numbers_with_missing_times.stream().
                    map(String::valueOf).
                    collect(Collectors.joining(", ")));
        }
    }

    @SuppressWarnings("IncorrectFormatting")
    private void printTimesWithMissingBibNumbers(final List<Duration> times_with_missing_bib_numbers) {

        if (!times_with_missing_bib_numbers.isEmpty()) {

            race.appendToNotes("""
                
                Times with missing bib numbers:
                
                """);

            race.appendToNotes(
                times_with_missing_bib_numbers.stream().
                    map(Normalisation::renderDuration).
                    collect(Collectors.joining(LINE_SEPARATOR)));
        }
    }
}
