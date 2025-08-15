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

import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.ResultPrinter;
import org.grahamkirby.race_timing_experimental.common.ResultsOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.grahamkirby.race_timing_experimental.common.Config.LINE_SEPARATOR;

@SuppressWarnings("preview")
public class RelayRaceResultsOutput implements ResultsOutput {

    @Override
    public void outputResults() throws IOException {

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printCollatedTimes();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public void setRace(Race race) {

        this.race = race;
        output_CSV = new RelayRaceOutputCSV(race);
        output_HTML = new RelayRaceOutputHTML(race);
        output_text = new RelayRaceOutputText(race);
        output_PDF = new RelayRaceOutputPDF(race);
    }

    private static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Race race;

    protected String year;
    protected String race_name_for_results;
    protected String race_name_for_filenames;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    RelayRaceOutputCSV output_CSV;
    RelayRaceOutputHTML output_HTML;
    RelayRaceOutputText output_text;
    RelayRaceOutputPDF output_PDF;

    protected void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();
    }

    private void printDetailedResults() throws IOException {

        output_CSV.printDetailedResults();
        output_HTML.printDetailedResults();
    }

    private void printLegResults() throws IOException {

        output_CSV.printLegResults();
        output_HTML.printLegResults();
    }

    private void printCollatedTimes() throws IOException {

        output_text.printCollatedResults();
    }

    protected void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    protected void printNotes() throws IOException {

        output_text.printNotes();
    }

    protected void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    /**
     * Prints overall race results. Used for CSV and HTML output.
     *
     * @throws IOException if an I/O error occurs.
     */
//    public void printResults() throws IOException {
//
//        final OutputStream stream = getOutputStream(race_name_for_filenames, "overall", year);
//
//        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
//
//            writer.append(getResultsHeader());
//            printResults(writer, getOverallResultPrinter(writer));
//        }
//    }

//    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
//        return new OverallResultPrinter(race, writer);
//    }

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    public String getResultsHeader() {
        return OVERALL_RESULTS_HEADER;
    }

    @SuppressWarnings("preview")
//    private static final class OverallResultPrinter extends ResultPrinter {
//
//        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
//            super(race, writer);
//        }
//
//        @Override
//        public void printResult(final RaceResult r) throws IOException {
//
//            RelayRaceResult result = (RelayRaceResult) r;
//            writer.append(STR."\{result.position_string},\{result.entry.bib_number},\{encode(result.entry.participant.name)},").
//                append(STR."\{encode(((Runner)result.entry.participant).club)},\{result.entry.participant.category.getShortName()},\{renderDuration(result, DNF_STRING)}\n");
//        }
//    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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
        return race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.csv");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints results using a specified printer, ordered by prize category groups. */
    protected void printResults(final OutputStreamWriter writer, final ResultPrinter printer) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategories().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(System.lineSeparator());
                writer.append(getResultsSubHeader(group.group_title()));
            }

            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));

            not_first_category_group = true;
        }
    }

    /** Formats a sub-header as appropriate for the output file type. */
    protected String getResultsSubHeader(final String s) {
        return LINE_SEPARATOR + s;
    }
}
