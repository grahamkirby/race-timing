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
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.*;

public abstract class RaceOutput {

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

    protected abstract String getFileSuffix();
    protected abstract String getResultsHeader() throws IOException;
    protected abstract String getPrizesSectionHeader();
    protected abstract String getPrizesCategoryHeader(final PrizeCategory category) ;
    protected abstract String getPrizesCategoryFooter();

    protected abstract ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer);
    protected abstract ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RaceOutput(final Race race) {

        this.race = race;
        configure();
    }

    public void printResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + getFileSuffix()));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getResultsHeader());
            printResults(writer, false);
        }
    }

    public void printPrizes() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(prizes_filename + getFileSuffix()));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printPrizes(writer);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    private void readProperties() {

        year = race.getProperty(KEY_YEAR);

        race_name_for_results = race.getProperty(KEY_RACE_NAME_FOR_RESULTS);
        race_name_for_filenames = race.getProperty(KEY_RACE_NAME_FOR_FILENAMES);
    }

    protected void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;
        notes_filename = "processing_notes";

        output_directory_path = race.getPath("../output");
    }

    protected String makeSubHeading(String s) {
        return s;
    }

    protected void printResults(final OutputStreamWriter writer, final boolean include_credit_link) throws IOException {

        int group_number = 0;
        for (final PrizeCategoryGroup group : race.prize_category_groups) {

            final String group_title = group.group_title();
            final List<PrizeCategory> prize_categories = group.categories();

            final String sub_heading = race.prize_category_groups.size() == 1 ? "" : "\n" + makeSubHeading(group_title);

            printResults(writer, prize_categories, sub_heading, include_credit_link && group_number++ == race.prize_category_groups.size() - 1);
        }
    }

    protected void printResults(final OutputStreamWriter writer, final List<PrizeCategory> categories, final String sub_heading, final boolean include_credit_link) throws IOException {

        writer.append(sub_heading);

        final List<RaceResult> results = race.getOverallResultsByCategory(categories);

        setPositionStrings(results, race.allowEqualPositions());
        getOverallResultPrinter(writer).print(results, include_credit_link);
    }

    protected void printPrizes(final OutputStreamWriter writer) throws IOException {

        writer.append(getPrizesSectionHeader());

        printPrizes(category -> {
            try {
                printPrizes(writer, category);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    protected void printPrizes(final Function<PrizeCategory, Void> prize_printer) {

        race.prize_category_groups.stream().
                filter(this::prizesInThisOrLaterGroup).
                flatMap(group -> group.categories().stream()).
                filter(this::prizesInThisOrLaterCategory).
                forEach(prize_printer::apply);
    }

    private void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) throws IOException {

        writer.append(getPrizesCategoryHeader(category));

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        setPositionStrings(category_prize_winners, race.allowEqualPositions());
        getPrizeResultPrinter(writer).print(category_prize_winners, false);

        writer.append(getPrizesCategoryFooter());
    }

    protected void setPositionStrings(final List<? extends RaceResult> results, final boolean allow_equal_positions) {

        // Sets position strings for dead heats, if allowed by the allow_equal_positions flag.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        // The flag is passed in rather than using race.allowEqualPositions() since that applies to the race overall.
        // In a series race the individual races don't allow equal positions, but the race overall does.
        // Conversely in a relay race the legs after the first leg do allow equal positions.

        for (int result_index = 0; result_index < results.size(); result_index++) {

            // Skip over any following results with the same performance.
            // Defined in terms of performance rather than duration, since in some races ranking is determined
            // by points rather than times.
            if (allow_equal_positions) {

                final int highest_index_with_same_performance = getHighestIndexWithSamePerformance(results, result_index);

                if (highest_index_with_same_performance > result_index) {

                    // Record the same position for all the results with equal times.
                    for (int i = result_index; i <= highest_index_with_same_performance; i++)
                        results.get(i).position_string = result_index + 1 + "=";

                    result_index = highest_index_with_same_performance;
                } else
                    setPositionStringByPosition(results, result_index);
            }
            else setPositionStringByPosition(results, result_index);
        }
    }

    private void setPositionStringByPosition(final List<? extends RaceResult> results, final int result_index) {
        results.get(result_index).position_string = String.valueOf(result_index + 1);
    }

    private boolean prizesInThisOrLaterGroup(final PrizeCategoryGroup group) {

        for (final PrizeCategoryGroup group2 : race.prize_category_groups.reversed()) {

            for (final PrizeCategory category : group2.categories())
                if (prizesInThisOrLaterCategory(category)) return true;

            if (group2.group_title().equals(group.group_title())) return false;
        }
        return false;
    }

    private boolean prizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory category2 : race.getPrizeCategories().reversed()) {

            if (!race.prize_winners.get(category2).isEmpty()) return true;
            if (category.equals(category2) && !prizesInOtherCategorySameAge(category)) return false;
        }
        return false;
    }

    private boolean prizesInOtherCategorySameAge(final PrizeCategory category) {

        for (final PrizeCategory c : race.getPrizeCategories())
            if (!c.equals(category) && c.getMinimumAge() == category.getMinimumAge() && !race.prize_winners.get(c).isEmpty()) return true;

        return false;
    }

    private int getHighestIndexWithSamePerformance(final List<? extends RaceResult> results, final int start_index) {

        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result + 1 < results.size() &&
                results.get(start_index).comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)
            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }
}
