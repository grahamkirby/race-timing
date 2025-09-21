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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;

public class SeriesRaceOutputCSV {

    static String getConcatenatedRaceNames(final List<Race> races) {

        return races.stream().
            filter(Objects::nonNull).
            map(race -> (String) race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS)).collect(Collectors.joining(","));
    }

    static void printResults(final Race race, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_result_printer) throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(race.getOutputDirectoryPath().resolve(race_name + "_overall_" + year + "." + CSV_FILE_SUFFIX), STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printResults(writer, make_result_printer.apply(race, writer), race);
        }
    }

    private static void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Race race) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers)
                if (not_first_category_group)
                    writer.append(LINE_SEPARATOR);

            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));

            not_first_category_group = true;
        }
    }
}
