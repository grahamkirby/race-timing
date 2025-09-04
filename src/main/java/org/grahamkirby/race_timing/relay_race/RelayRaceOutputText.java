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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

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

        final String race_name = (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(Config.KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(getOutputFilePath(race_name, "prizes", year), Config.STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeader());

            race.getCategoryDetails().getPrizeCategoryGroups().stream().
                flatMap(group -> group.categories().stream()).                       // Get all prize categories.
                filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
                forEachOrdered(category -> printPrizes(writer, category));                       // Print prizes in this category.
        }
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    void printNotes() throws IOException {

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);

        final String race_name = (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(Config.KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(getOutputFilePath(race_name, "processing_notes", year), Config.STANDARD_FILE_OPEN_OPTIONS))) {
            writer.append(race.getNotes());
        }
    }

    /**
     * Constructs a path for a file in the project output directory with name constructed from the given components.
     */
    Path getOutputFilePath(final String race_name, final String output_type, final String year) {

        return race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.\{Config.TEXT_FILE_SUFFIX}");
    }

    /** Prints prizes within a given category. */
    private void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            writer.append(getPrizeCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            new PrizeResultPrinter(race, writer).print(category_prize_winners);

            writer.append(Config.LINE_SEPARATOR).append(Config.LINE_SEPARATOR);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPrizeCategoryHeader(final PrizeCategory category) {

        final String header = STR."Category: \{category.getLongName()}";
        return STR."""
            \{header}
            \{"-".repeat(header.length())}

            """;
    }

    private String getPrizesHeader() {

        final String header = STR."\{(String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_RESULTS)} Results \{(String) race.getConfig().get(Config.KEY_YEAR)}";
        return STR."""
            \{header}
            \{"=".repeat(header.length())}

            """;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printCollatedResults() throws IOException {

        final String race_name = (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(Config.KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(getOutputFilePath(race_name, "times_collated", year), Config.STANDARD_FILE_OPEN_OPTIONS);

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

        writer.append(bib_number != Config.UNKNOWN_BIB_NUMBER ? String.valueOf(bib_number) : "?").
            append("\t").
            append(raw_result.getRecordedFinishTime() != null ? Normalisation.format(raw_result.getRecordedFinishTime()) : "?");
    }

    private void printLegNumber(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataImpl)race.getRaceData()).explicitly_recorded_leg_numbers;

        if (explicitly_recorded_leg_numbers.containsKey(raw_result)) {

            final int leg_number = explicitly_recorded_leg_numbers.get(raw_result);

            writer.append("\t").append(String.valueOf(leg_number));

            if (legs_already_finished >= leg_number)
                raw_result.appendComment(STR."Leg \{leg_number} finisher was runner \{legs_already_finished + 1} to finish for team.");
        }
    }

    private void printComment(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataImpl)race.getRaceData()).explicitly_recorded_leg_numbers;

        if (!raw_result.getComment().isEmpty()) {

            if (!explicitly_recorded_leg_numbers.containsKey(raw_result)) writer.append("\t");
            writer.append("\t").append(Config.COMMENT_SYMBOL).append(" ").append(raw_result.getComment());
        }

        writer.append(Config.LINE_SEPARATOR);
    }

    @SuppressWarnings("IncorrectFormatting")
    private void printBibNumbersWithMissingTimes(final List<Integer> bib_numbers_with_missing_times) {

        if (!bib_numbers_with_missing_times.isEmpty()) {

            race.appendToNotes("""
                
                Bib numbers with missing times:\s""");

            race.appendToNotes(
                bib_numbers_with_missing_times.stream().
                    map(String::valueOf).
                    reduce((i1, i2) -> STR."\{i1}, \{i2}").
                    orElse(""));
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
                    map(Normalisation::format).
                    reduce((i1, i2) -> STR."\{i1}\n\{i2}").
                    orElse(""));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class PrizeResultPrinter extends ResultPrinterText {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            RelayRaceResult result = (RelayRaceResult) r;
            writer.append(STR."\{result.position_string}: \{result.entry.participant.name} (\{result.entry.participant.category.getLongName()}) \{Config.renderDuration(result, Config.DNF_STRING)}\n");
        }
    }
}
