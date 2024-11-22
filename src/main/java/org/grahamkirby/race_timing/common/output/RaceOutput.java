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
            printResults(writer, getOverallResultPrinter(writer), false);
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

    protected void printResults(final OutputStreamWriter writer, ResultPrinter printer, final boolean include_credit_link) throws IOException {

        for (int i = 0; i < race.prize_category_groups.size(); i++) {

            final PrizeCategoryGroup group = race.prize_category_groups.get(i);
            final String group_title = group.group_title();
            final List<PrizeCategory> prize_categories = group.categories();

            final boolean only_one_group = race.prize_category_groups.size() == 1;
            final boolean last_group = i == race.prize_category_groups.size() - 1;

            final String sub_heading = only_one_group ? "" : "\n" + makeSubHeading(group_title);

            printResults(writer, printer, prize_categories, sub_heading, include_credit_link && last_group);
        }
    }

    protected void printResults(final OutputStreamWriter writer, ResultPrinter printer, final List<PrizeCategory> categories, final String sub_heading, final boolean include_credit_link) throws IOException {

        writer.append(sub_heading);

        final List<RaceResult> results = race.getOverallResults(categories);

        printer.print(results, include_credit_link);
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

        final List<RaceResult> category_prize_winners = race.getPrizeWinners(category);

        getPrizeResultPrinter(writer).print(category_prize_winners, false);

        writer.append(getPrizesCategoryFooter());
    }

    // TODO move prize logic to prizes class
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
}
