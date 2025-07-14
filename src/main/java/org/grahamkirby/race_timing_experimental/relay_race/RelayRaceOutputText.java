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
package org.grahamkirby.race_timing_experimental.relay_race;


import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResult;
import org.grahamkirby.race_timing_experimental.common.ResultPrinter;
import org.grahamkirby.race_timing_experimental.common.ResultPrinterText;
import org.grahamkirby.race_timing_experimental.common.Normalisation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;
import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;

import static org.grahamkirby.race_timing_experimental.common.Config.*;
import static org.grahamkirby.race_timing_experimental.common.Normalisation.format;
import static org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputCSV.renderDuration;

/** Base class for plaintext output. */
@SuppressWarnings("preview")
public class RelayRaceOutputText {

    private final Race race;

    protected RelayRaceOutputText(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected String getFileSuffix() {
        return "txt";
    }

    /** No headings in plaintext file. */
    protected String getResultsHeader() {
        return "";
    }

    private static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    /** Prints out the words converted to title case, and any other processing notes. */
    public void printNotes() throws IOException {

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "processing_notes", (String) race.getConfig().get(KEY_YEAR)))) {
            writer.append(race.getNotes());
        }
    }

    /**
     * Constructs an output stream for writing to a file in the project output directory with name constructed from the given components.
     * The file extension is determined by getFileSuffix().
     * The file is created if it does not already exist, and overwritten if it does.
     * Example file name: "balmullo_prizes_2023.html".
     *
     * @param race_name the name of the race in format suitable for inclusion with file name
     * @param output_type the type of output file e.g. "overall", "prizes" etc.
     * @param year the year of the race
     * @return an output stream for the file
     * @throws IOException if an I/O error occurs
     */
    protected OutputStream getOutputStream(final String race_name, final String output_type, final String year) throws IOException {

        return getOutputStream(race_name, output_type, year, STANDARD_FILE_OPEN_OPTIONS);
    }

    /** As {@link #getOutputStream(String, String, String)} with specified file creation options. */
    protected OutputStream getOutputStream(final String race_name, final String output_type, final String year, final OpenOption... options) throws IOException {

        return Files.newOutputStream(getOutputFilePath(race_name, output_type, year), options);
    }

    /**
     * Constructs a path for a file in the project output directory with name constructed from the given components.
     * The file extension is determined by getFileSuffix().
     * Example file name: "balmullo_prizes_2023.html".
     *
     * @param race_name the name of the race in format suitable for inclusion with file name
     * @param output_type the type of output file e.g. "overall", "prizes" etc.
     * @param year the year of the race
     * @return the path for the file
     */
    Path getOutputFilePath(final String race_name, final String output_type, final String year) {

        return race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.\{getFileSuffix()}");
    }

    /**
     * Prints race prizes. Used for HTML and text output.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void printPrizes() throws IOException {

        final OutputStream stream = getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "prizes", (String) race.getConfig().get(KEY_YEAR));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeader());
            printPrizes(writer);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    void printPrizes(final OutputStreamWriter writer) {

        printPrizes(category -> {
            printPrizes(writer, category);
            return null;
        });
    }

    /**
     * Prints prizes using a specified printer, ordered by prize category groups.
     * The printer abstracts over whether output goes to an output stream writer
     * (CSV, HTML and text files) or to a PDF writer.
     */
    void printPrizes(final Function<? super PrizeCategory, Void> prize_category_printer) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                       // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
            forEachOrdered(prize_category_printer::apply);                       // Print prizes in this category.
    }

    /** Prints prizes within a given category. */
    private void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            writer.append(getPrizeCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            getPrizeResultPrinter(writer).print(category_prize_winners);

            writer.append(getPrizeCategoryFooter());
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getPrizeCategoryHeader(final PrizeCategory category) {

        final String header = STR."Category: \{category.getLongName()}";
        return STR."""
            \{header}
            \{"-".repeat(header.length())}

            """;
    }

    protected String getPrizeCategoryFooter() {
        return LINE_SEPARATOR + LINE_SEPARATOR;
    }

    protected String getPrizesHeader() {

        final String header = STR."\{(String) race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS)} Results \{(String) race.getConfig().get(KEY_YEAR)}";
        return STR."""
            \{header}
            \{"=".repeat(header.length())}

            """;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Full results not printed to text file.
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }

    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    void printCollatedResults() throws IOException {
        final OutputStream stream = getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "times_collated", (String) race.getConfig().get(KEY_YEAR));


        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final Map<Integer, Integer> legs_finished_per_team = ((RelayRaceImpl)race.getSpecific()).countLegsFinishedPerTeam();

            printResults(writer, legs_finished_per_team);

            final List<Integer> bib_numbers_with_missing_times = ((RelayRaceImpl)race.getSpecific()).getBibNumbersWithMissingTimes(legs_finished_per_team);
            final List<Duration> times_with_missing_bib_numbers = ((RelayRaceImpl)race.getSpecific()).getTimesWithMissingBibNumbers();

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
            append(raw_result.getRecordedFinishTime() != null ? format(raw_result.getRecordedFinishTime()) : "?");
    }

    private void printLegNumber(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {
        Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataProcessorImpl)race.getRaceData()).explicitly_recorded_leg_numbers;

        if (explicitly_recorded_leg_numbers.containsKey(raw_result)) {
//            if (raw_result.getLegNumber() > 0) {
            int leg_number = explicitly_recorded_leg_numbers.get(raw_result);

            writer.append("\t").append(String.valueOf(leg_number));

            if (legs_already_finished >= leg_number)
                raw_result.appendComment(STR."Leg \{leg_number} finisher was runner \{legs_already_finished + 1} to finish for team.");
        }
    }

    private  void printComment(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {
        Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataProcessorImpl)race.getRaceData()).explicitly_recorded_leg_numbers;

        if (!raw_result.getComment().isEmpty()) {

//            if (raw_result.getLegNumber() == 0) writer.append("\t");
            if (!explicitly_recorded_leg_numbers.containsKey(raw_result)) writer.append("\t");
            writer.append("\t").append(org.grahamkirby.race_timing.common.Race.COMMENT_SYMBOL).append(" ").append(raw_result.getComment());
        }

        writer.append(LINE_SEPARATOR);
    }

    @SuppressWarnings("IncorrectFormatting")
    private void printBibNumbersWithMissingTimes(final Collection<Integer> bib_numbers_with_missing_times) {

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
    private void printTimesWithMissingBibNumbers(final Collection<Duration> times_with_missing_bib_numbers) {

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

            writer.append(STR."\{result.position_string}: \{result.entry.participant.name} (\{((Runner) result.entry.participant).club}) \{renderDuration(result)}\n");
        }
    }
}
