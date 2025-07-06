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
package org.grahamkirby.race_timing_experimental.individual_race;


import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing_experimental.common.Race;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_FILENAMES;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;
import static org.grahamkirby.race_timing.common.output.RaceOutputHTML.SOFTWARE_CREDIT_LINK_TEXT;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_YEAR;

public class IndividualRaceOutputHTML {

    /** Displayed in results for runners that did not complete the course. */
    public static final String DNF_STRING = "DNF";
    private static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    private final Race race;

    IndividualRaceOutputHTML(final Race race) {
        this.race = race;
    }


    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    public void printResults() throws IOException {

        final OutputStream stream = getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "overall", (String) race.getConfig().get(KEY_YEAR));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getResultsHeader());
            printResults(writer, getOverallResultPrinter(writer));
        }
    }

    /** Prints all details to a single web page. */
    public void printCombined() throws IOException {

        printCombinedDetails();
        printCreditLink();
    }

    /** Prints prizes and overall results to a single web page. */
    protected void printCombinedDetails() throws IOException {

        final OutputStream stream = getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "combined", (String) race.getConfig().get(KEY_YEAR));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(getPrizesHeader());
            printPrizes(writer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            printResults(writer, getOverallResultPrinter(writer));
        }
    }

    /** Prints web link to GitHub page for this application. */
    private void printCreditLink() throws IOException {

        final OutputStream stream = getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "combined", (String) race.getConfig().get(KEY_YEAR), StandardOpenOption.APPEND);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

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

            final List<IndividualRaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            getPrizeResultPrinter(writer).print(category_prize_winners);

            writer.append(getPrizeCategoryFooter());
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFileSuffix() {
        return ".html";
    }

    public String getResultsHeader() {
        return "";
    }

    public String getResultsSubHeader(final String s) {
        return STR."""
            <p></p>
            <h4>\{s}</h4>
            """;
    }

    public String getPrizesHeader() {
        return STR."<h4>Prizes</h4>\{LINE_SEPARATOR}";
    }

    public String getPrizeCategoryHeader(final PrizeCategory category) {
        return STR."""
        <p><strong>\{category.getLongName()}</strong></p>
        """;
    }

    public String getPrizeCategoryFooter() {
        return "";
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

        return race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.html");
    }

    /** Prints results using a specified printer, ordered by prize category groups. */
    protected void printResults(final OutputStreamWriter writer, final ResultPrinter printer) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(System.lineSeparator());
                writer.append(getResultsSubHeader(group.group_title()));
            }

            IndividualRaceResultsCalculator raceResults = race.getResultsCalculator();
            List<IndividualRaceResult> overallResults = raceResults.getOverallResults(group.categories());
            printer.print(overallResults);

            not_first_category_group = true;
        }
    }

    /** Encodes a single value by surrounding with quotes if it contains a comma. */
    public static String encode(final String s) {
        return s.contains(",") ? STR."\"\{s}\"" : s;
    }

    public static String renderDuration(final Duration duration, final String alternative) {

        return duration != null ? format(duration) : alternative;
    }

    public static String renderDuration(final IndividualRaceResult result, final String alternative) {

        return IndividualRaceOutputCSV.renderDuration(result, alternative);
    }

    public static String renderDuration(final IndividualRaceResult result) {
        return renderDuration(result, "");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Club", "Category", "Time");
        }

        @Override
        protected List<String> getResultsElements(final IndividualRaceResult result) {

            return List.of(
                result.position_string,
                String.valueOf(result.entry.bib_number),
                race.getNormalisation().htmlEncode(result.entry.participant.name),
                ((Runner)result.entry.participant).club,
                result.entry.participant.category.getShortName(),
                renderDuration(result, DNF_STRING)
            );
        }
    }

    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }
    private static final class PrizeResultPrinter extends ResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final IndividualRaceResult result) throws IOException {


            writer.append(STR."    <li>\{result.position_string} \{race.getNormalisation().htmlEncode(result.entry.participant.name)} (\{((Runner)result.entry.participant).club}) \{renderDuration(result)}</li>\n");
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
    }
}
