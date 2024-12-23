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
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.*;

/**
 * Abstract parent class for various forms of race output.
 */
@SuppressWarnings("IncorrectFormatting")
public abstract class RaceOutput {

    public static final String DNF_STRING = "DNF";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected final Race race;

    protected Path output_directory_path;
    protected String year;
    protected String race_name_for_results;
    protected String race_name_for_filenames;
    private String overall_results_filename;
    String prizes_filename;
    String notes_filename;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract String getFileSuffix();
    protected abstract String getResultsHeader();
    protected abstract String getPrizesSectionHeader();
    protected abstract String getPrizesCategoryHeader(final PrizeCategory category) ;
    protected abstract String getPrizesCategoryFooter();

    protected abstract ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer);
    protected abstract ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    RaceOutput(final Race race) {

        this.race = race;
        configure();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void printResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + getFileSuffix()));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getResultsHeader());
            printResults(writer, getOverallResultPrinter(writer), CreditLinkOption.DO_NOT_INCLUDE_CREDIT_LINK);
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

        overall_results_filename = STR."\{race_name_for_filenames}_overall_\{year}";
        prizes_filename = STR."\{race_name_for_filenames}_prizes_\{year}";
        notes_filename = STR."\{race_name_for_filenames}_processing_notes_\{year}";

        output_directory_path = race.getPath("../output");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final CreditLinkOption credit_link_option) throws IOException {

        for (int i = 0; i < race.prize_category_groups.size(); i++) {

            final PrizeCategoryGroup group = race.prize_category_groups.get(i);
            final String group_title = group.group_title();
            final List<PrizeCategory> prize_categories = group.categories();

            final boolean only_one_group = race.prize_category_groups.size() == 1;
            final boolean last_group = i == race.prize_category_groups.size() - 1;

            final String sub_heading = only_one_group ? "" : LINE_SEPARATOR + makeSubHeading(group_title);
            final boolean output_credit_link = last_group && credit_link_option == CreditLinkOption.INCLUDE_CREDIT_LINK;

            printResults(writer, printer, prize_categories, sub_heading, output_credit_link ? CreditLinkOption.INCLUDE_CREDIT_LINK : CreditLinkOption.DO_NOT_INCLUDE_CREDIT_LINK);
        }
    }

    void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Collection<PrizeCategory> categories, final String sub_heading, final CreditLinkOption credit_link_option) throws IOException {

        writer.append(sub_heading);

        final List<RaceResult> results = race.getOverallResults(categories);

        printer.print(results, credit_link_option);
    }

    protected String makeSubHeading(final String s) {
        return s;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void printPrizes(final OutputStreamWriter writer) throws IOException {

        writer.append(getPrizesSectionHeader());

        printPrizes(category -> {
            printPrizes(writer, category);
            return null;
        });
    }

    void printPrizes(final Function<? super PrizeCategory, Void> prize_printer) {

        race.prize_category_groups.stream().
            flatMap(group -> group.categories().stream()).
            filter(race.prizes::arePrizesInThisOrLaterCategory).
            forEachOrdered(prize_printer::apply);
    }

    private void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            writer.append(getPrizesCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.prizes.getPrizeWinners(category);
            getPrizeResultPrinter(writer).print(category_prize_winners, CreditLinkOption.DO_NOT_INCLUDE_CREDIT_LINK);

            writer.append(getPrizesCategoryFooter());

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
