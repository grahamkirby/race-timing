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
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.SUFFIX_CSV;

public abstract class RaceOutputCSV extends RaceOutput {

    public RaceOutputCSV(Race race) {
        super(race);
    }

    @Override
    public void printResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + SUFFIX_CSV);

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printResultsHeader(writer);
            printResults(writer);
        }
    }

    private void printResults(final OutputStreamWriter writer) throws IOException {

        for (final PrizeCategoryGroup category_group : race.prize_category_groups)
            printCategoryResults(writer, category_group.categories());
    }

    private void printCategoryResults(final OutputStreamWriter writer, final List<PrizeCategory> categories) throws IOException {

        final List<RaceResult> results = race.getOverallResultsByCategory(categories);

        setPositionStrings(results, race.allowEqualPositions());
//        printResults(results, getOverallResultPrinter(writer));
        getOverallResultPrinter(writer).print(results, false);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void printResultsHeader(final OutputStreamWriter writer) throws IOException;

    // Prizes not output to CSV files.
    @Override
    public void printPrizes() { throw new UnsupportedOperationException(); }

    // Prizes not output to CSV files.
    @Override
    protected void printPrizesInCategory(OutputStreamWriter writer, PrizeCategory category) { throw new UnsupportedOperationException(); }
}
