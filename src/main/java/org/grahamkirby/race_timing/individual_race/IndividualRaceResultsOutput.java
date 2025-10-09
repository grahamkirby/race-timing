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

import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.ResultPrinter;
import org.grahamkirby.race_timing.common.ResultPrinterGenerator;
import org.grahamkirby.race_timing.common.ResultsOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.*;

@SuppressWarnings("preview")
public class IndividualRaceResultsOutput implements ResultsOutput {

    private IndividualRaceOutputCSV output_CSV;
    private IndividualRaceOutputHTML output_HTML;
    private IndividualRaceOutputText output_text;
    private IndividualRaceOutputPDF output_PDF;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void outputResults() throws IOException {

        printOverallResults();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public void setRace(final Race race) {

        output_CSV = new IndividualRaceOutputCSV(race);
        output_HTML = new IndividualRaceOutputHTML(race);
        output_text = new IndividualRaceOutputText(race);
        output_PDF = new IndividualRaceOutputPDF(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();
    }

    private void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    private void printNotes() throws IOException {

        output_text.printNotes();
    }

    private void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    /** Prints results using a specified printer, ordered by prize category groups. */
    public static void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header, final Race race) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(LINE_SEPARATOR);
                writer.append(get_results_sub_header.apply(group.group_title()));
            }

            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));

            not_first_category_group = true;
        }
    }

    public static Path getOutputStreamPath(final Race race, final String output_type, final String file_suffix) {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        return race.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + file_suffix);
    }

    public static OutputStream getOutputStream(final Race race, final String output_type, final String file_suffix) throws IOException {

        return getOutputStream(race, output_type, file_suffix, STANDARD_FILE_OPEN_OPTIONS);
    }

    public static OutputStream getOutputStream(final Race race, final String output_type, final String file_suffix, final OpenOption[] file_open_options) throws IOException {

        return Files.newOutputStream(getOutputStreamPath(race, output_type, file_suffix), file_open_options);
    }

    public static void printResults(final Race race, final ResultPrinterGenerator make_result_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race, "overall", HTML_FILE_SUFFIX))) {

            final ResultPrinter printer = make_result_printer.apply(race, writer);
            printResults(writer, printer, IndividualRaceOutputHTML::getResultsSubHeader, race);
        }
    }
}
