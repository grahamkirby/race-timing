/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.*;

/** Abstract parent class for various forms of race output. */
@SuppressWarnings("IncorrectFormatting")
public abstract class RaceOutput {

    /** Displayed in results for runners that did not complete the course. */
    public static final String DNF_STRING = "DNF";

    private static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected final Race race;

    protected String year;
    protected String race_name_for_results;
    protected String race_name_for_filenames;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract String getFileSuffix();
    protected abstract String getResultsHeader();
    protected abstract String getPrizesHeader();
    protected abstract String getPrizeCategoryHeader(final PrizeCategory category) ;
    protected abstract String getPrizeCategoryFooter();

    protected abstract ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer);
    protected abstract ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    RaceOutput(final Race race) {

        this.race = race;
        readProperties();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Prints overall race results. Used for CSV and HTML output.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void printResults() throws IOException {

        final OutputStream stream = getOutputStream(race_name_for_filenames, "overall", year);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getResultsHeader());
            printResults(writer, getOverallResultPrinter(writer));
        }
    }

    /**
     * Prints race prizes. Used for HTML and text output.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void printPrizes() throws IOException {

        final OutputStream stream = getOutputStream(race_name_for_filenames, "prizes", year);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeader());
            printPrizes(writer);
        }
    }

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
        return race.getPath("../output").resolve(STR."\{race_name}_\{output_type}_\{year}\{getFileSuffix()}");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void readProperties() {

        year = race.getProperty(KEY_YEAR);

        race_name_for_results = race.getProperty(KEY_RACE_NAME_FOR_RESULTS);
        race_name_for_filenames = race.getProperty(KEY_RACE_NAME_FOR_FILENAMES);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints results using a specified printer, ordered by prize category groups. */
    protected void printResults(final OutputStreamWriter writer, final ResultPrinter printer) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.prize_category_groups.size() > 1;

        for (final PrizeCategoryGroup group : race.prize_category_groups) {

            if (should_display_category_group_headers)
                writer.append(getResultsSubHeader(group.group_title()));

            printer.print(race.getOverallResults(group.categories()));
        }
    }

    /** Formats a sub-header as appropriate for the output file type. */
    protected String getResultsSubHeader(final String s) {
        return LINE_SEPARATOR + s;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Prints prizes using a specified printer, ordered by prize category groups.
     * The printer abstracts over whether output goes to an output stream writer
     * (CSV, HTML and text files) or to a PDF writer.
     */
    void printPrizes(final Function<? super PrizeCategory, Void> prize_category_printer) {

        race.prize_category_groups.stream().
            flatMap(group -> group.categories().stream()).       // Get all prize categories.
            filter(race.prizes::arePrizesInThisOrLaterCategory). // Discard further categories once all prizes have been output.
            forEachOrdered(prize_category_printer::apply);       // Print prizes in this category.
    }

    /** Prints prizes, ordered by prize category groups. */
    void printPrizes(final OutputStreamWriter writer) {

        printPrizes(category -> {
            printPrizes(writer, category);
            return null;
        });
    }

    /** Prints prizes within a given category. */
    private void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            writer.append(getPrizeCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.prizes.getPrizeWinners(category);
            getPrizeResultPrinter(writer).print(category_prize_winners);

            writer.append(getPrizeCategoryFooter());

        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
