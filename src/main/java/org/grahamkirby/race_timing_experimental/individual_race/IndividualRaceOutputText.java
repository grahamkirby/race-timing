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
import org.grahamkirby.race_timing_experimental.common.Race;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_FILENAMES;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_YEAR;
import static org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputCSV.renderDuration;

/** Base class for plaintext output. */
public class IndividualRaceOutputText {

    public static String debug_info = "DEBUG2";

    private final Race race;

    protected IndividualRaceOutputText(final Race race) {
        this.race= race;
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
            writer.append(race.getNotes().toString());
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

        Path resolve = race.getFullPath("../output").resolve(STR."\{race_name}_\{output_type}_\{year}.\{getFileSuffix()}");
//        Path resolve = race.getFullPath("../output").resolve(STR."\{race_name}_\{output_type}_\{year}.\{getFileSuffix()}");
debug_info = resolve.toString();
        return resolve;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Full results not printed to text file.
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }

    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class PrizeResultPrinter extends ResultPrinterText {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final IndividualRaceResult result) throws IOException {

            writer.append(STR."\{result.position_string}: \{result.entry.participant.name} (\{((Runner) result.entry.participant).club}) \{renderDuration(result)}\n");
        }
    }
}
