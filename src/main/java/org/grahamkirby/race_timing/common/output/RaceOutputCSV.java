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
import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class RaceOutputCSV extends RaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public RaceOutputCSV(Race race) {
        super(race);
    }

    @Override
    public void printOverallResults(boolean include_credit_link) throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsHeader(writer);
            printOverallResults(writer);
        }
    }

    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

        for (final Race.CategoryGroup category_group : race.getResultCategoryGroups())
            printCategoryResults(writer, category_group.category_names());
    }

    protected void printCategoryResults(final OutputStreamWriter writer, final List<String> category_names) throws IOException {

        final List<Category> category_list = category_names.stream().map(s -> race.categories.getCategory(s)).toList();
        final List<RaceResult> results = race.getResultsByCategory(category_list);

        setPositionStrings(results, allowEqualPositions());
        printResults(results, getResultPrinter(writer));
    }

    protected void printOverallResultsHeaderRootSeries(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : ((SeriesRace)race).getRaces())
            if (individual_race != null)
                writer.append(",").
                        append(individual_race.getProperties().getProperty("RACE_NAME_FOR_RESULTS"));
    }
}
