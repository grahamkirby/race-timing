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
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_FILENAMES;
import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_RESULTS;

public abstract class RaceOutput {

    public interface ResultPrinter {

        void printResult(RaceResult result) throws IOException;
        void printNoResults() throws IOException;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String DNF_STRING = "DNF";

    protected final Race race;

    protected String year;
    protected String race_name_for_results;
    protected String race_name_for_filenames;
    protected String overall_results_filename;
    protected String prizes_filename;
    protected String notes_filename;
    protected Path output_directory_path;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RaceOutput(final Race race) {

        this.race = race;
        configure();
    }

    protected void printResults(final List<RaceResult> results, final ResultPrinter printer) throws IOException {

        for (final RaceResult result : results)
            printer.printResult(result);

        if (results.isEmpty())
            printer.printNoResults();
    }

    protected void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;
        notes_filename = "processing_notes";

        output_directory_path = race.getPath("../output");
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    private void readProperties() {

        year = race.getProperty("YEAR");

        race_name_for_results = race.getProperty(KEY_RACE_NAME_FOR_RESULTS);
        race_name_for_filenames = race.getProperty(KEY_RACE_NAME_FOR_FILENAMES);
    }

    protected void setPositionStrings(final List<? extends RaceResult> results, final boolean allow_equal_positions) {

        // Sets position strings for dead heats.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        for (int result_index = 0; result_index < results.size(); result_index++) {

            // Skip over any following results with the same results.
            final int highest_index_with_same_duration = getHighestIndexWithSamePerformance(results, result_index);

            if (allow_equal_positions && highest_index_with_same_duration > result_index) {

                // Record the same position for all the results with equal times.
                for (int i = result_index; i <= highest_index_with_same_duration; i++)
                    results.get(i).position_string = result_index + 1 + "=";

                result_index = highest_index_with_same_duration;
            }
            else
                results.get(result_index).position_string = String.valueOf(result_index + 1);
        }
    }

    protected boolean prizesInThisOrLaterGroup(final PrizeCategoryGroup group) {

        for (final PrizeCategoryGroup g : race.prize_category_groups.reversed()) {

            for (final PrizeCategory c : g.categories()) {
                if (prizesInThisOrLaterCategory(c)) return true;
            }
            if (g.combined_categories_title().equals(group.combined_categories_title())) return false;
        }
        return false;
    }

    protected boolean prizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory c : race.getPrizeCategories().reversed()) {

            if (!race.prize_winners.get(c).isEmpty()) return true;
            if (category.equals(c) && !prizesInOtherCategorySameAge(category)) return false;
        }
        return false;
    }

    protected boolean prizesInOtherCategorySameAge(final PrizeCategory category) {

        final int age = category.getMinimumAge();

        for (final PrizeCategory c : race.getPrizeCategories()) {
            if (!c.equals(category) && age == c.getMinimumAge() && !race.prize_winners.get(c).isEmpty()) return true;
        }

        return false;
    }

    private int getHighestIndexWithSamePerformance(final List<? extends RaceResult> results, final int start_index) {

        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result + 1 < results.size() &&
                results.get(start_index).comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)
            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void printResults() throws IOException;
    public abstract void printPrizes() throws IOException;

    protected abstract void printPrizesInCategory(final OutputStreamWriter writer, final PrizeCategory category) throws IOException;
    protected abstract ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer);
    protected abstract ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer);
}
