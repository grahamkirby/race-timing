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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.individual_race.IndividualRaceOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Config.STANDARD_FILE_OPEN_OPTIONS;

public class RaceOutput {

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

    /** Prints prizes, ordered by prize category groups. */
    public static void printPrizes(final Race race, final OutputStreamWriter writer, final ResultPrinterGenerator make_prize_result_printer) throws IOException {

        final ResultPrinter printer = make_prize_result_printer.apply(race, writer);

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                        // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).                    // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(category, race, writer, printer));
    }

    /** Prints prizes within a given category. */
    private static void printPrizes(final PrizeCategory category, final Race race, final OutputStreamWriter writer, final ResultPrinter printer) {

        try {
            writer.append("<p><strong>" + category.getLongName() + "</strong></p>" + LINE_SEPARATOR);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            printer.print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
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

    public static void printResults(final Race race, final ResultPrinterGenerator make_result_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race, "overall", HTML_FILE_SUFFIX))) {

            final ResultPrinter printer = make_result_printer.apply(race, writer);
            printResults(writer, printer, IndividualRaceOutput::getResultsSubHeaderHTML, race);
        }
    }
}
