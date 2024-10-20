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
package org.grahamkirby.race_timing.series_race.fife_ac_minitour;

import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;
import org.grahamkirby.race_timing.series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class MinitourRaceOutputHTML extends RaceOutputHTML {

    public MinitourRaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void printOverallResults(final OutputStreamWriter writer, boolean include_credit_link) throws IOException {

        writer.append("<h4>Overall Results</h4>\n");

        final List<Race.CategoryGroup> groups = race.getResultCategoryGroups();

        for (int i = 0; i < groups.size(); i++)
            printOverallResultsHTML(writer, groups.get(i).combined_categories_title(), groups.get(i).category_names(), i == groups.size() - 1);
    }

    @Override
    public void printCombined() throws IOException {

        for (int i = 1; i <= ((SeriesRace)race).getRaces().size(); i++)
            printIndividualRace(i);

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("""
                    <h3><strong>Results</strong></h3>
                    """);

            printPrizes(writer);
            printOverallResults(writer, true);
        }
    }

    @Override
    protected void printPrizes(final OutputStreamWriter writer, final Category category) throws IOException {

        // TODO make consistent with MidweekRaceOutputHTML
        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        writer.append("<p><strong>").
                append(category.getShortName()).
                append("</strong></p>\n");

        if (!category_prize_winners.isEmpty()) {

            writer.append("<ul>\n");

            setPositionStrings(category_prize_winners, true);
            printResults(category_prize_winners, new PrizeResultPrinterHTML(((MinitourRace)race), writer));

            writer.append("</ul>\n\n");
        }
        else {
            writer.append("<p>No results</p>\n");
        }
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>Runner</th>
                                       <th>Cat</th>
                                       <th>Club</th>
            """);

        final List<IndividualRace> races = ((SeriesRace) race).getRaces();

        for (int i = 0; i < races.size(); i++)
            if (races.get(i) != null)
                writer.append("<th>Race ").append(String.valueOf(i + 1)).append("</th>\n");

        writer.append("""
                                       <th>Total</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResultsHTML(final OutputStreamWriter writer, final String combined_categories_title, final List<String> category_names, boolean include_credit_link) throws IOException {

        final List<Category> category_list = category_names.stream().map(s -> race.categories.getCategory(s)).toList();

        writer.append("<h4>").append(combined_categories_title).append("</h4>\n");

        printOverallResultsHeader(writer);
        printOverallResultsBody(writer, category_list);
        printOverallResultsFooter(writer, include_credit_link);
    }

    private void printIndividualRace(final int race_number) throws IOException {

        final IndividualRace individual_race = ((SeriesRace)race).getRaces().get(race_number - 1);

        if (individual_race != null) {

            final OutputStream race_stream = Files.newOutputStream(output_directory_path.resolve("race" + race_number + ".html"));

            try (final OutputStreamWriter html_writer = new OutputStreamWriter(race_stream)) {

                printRaceCategories(html_writer, individual_race, "U9", "FU9","MU9");
                printRaceCategories(html_writer, individual_race, "U11", "FU11", "MU11");
                printRaceCategories(html_writer, individual_race, "U13", "FU13", "MU13");
                printRaceCategories(html_writer, individual_race, "U15", "FU15", "MU15");
                printRaceCategories(html_writer, individual_race, "U18", "FU18", "MU18");
            }
        }
    }

    private void printRaceCategories(final OutputStreamWriter writer, final Race race, final String combined_categories_title, final String... category_names) throws IOException {

        final List<Category> category_list = getCategoryList(category_names);

        final List<RaceResult> category_results =
                race.getOverallResults().
                stream().
                filter(result -> category_list.contains(((IndividualRaceResult)result).entry.runner.category)).
                toList();

        printRaceCategories(writer, category_results, combined_categories_title);
    }

    private List<Category> getCategoryList(final String... category_names) {

        return Arrays.stream(category_names).map(s -> race.categories.getCategory(s)).toList();
    }

    private void printRaceCategories(final OutputStreamWriter writer, final List<RaceResult> category_results, final String combined_categories_title) throws IOException {

        writer.append("<h4>").
                append(combined_categories_title).
                append("</h4>\n").
                append("""
                    <table class="fac-table">
                                   <thead>
                                       <tr>
                                           <th>Pos</th>
                                           <th>No</th>
                                           <th>Runner</th>
                                           <th>Category</th>
                                           <th>Total</th>
                                       </tr>
                                   </thead>
                                   <tbody>
                """);

        printRaceCategories(writer, category_results);

        writer.append("""
                    </tbody>
                </table>
                """);
    }

    private void printRaceCategories(final OutputStreamWriter writer, final List<RaceResult> category_results) throws IOException {

        int position = 1;

        for (final RaceResult res : category_results) {

            final IndividualRaceResult result = (IndividualRaceResult) res;

            writer.append("""
                    <tr>
                        <td>""");

            if (!result.DNF) writer.append(String.valueOf(position++));

            writer.append("""
                    </td>
                    <td>""").
            append(String.valueOf(result.entry.bib_number)).
            append("""
                    </td>
                    <td>""").
            append(race.normalisation.htmlEncode(result.entry.runner.name)).
            append("""
                    </td>
                    <td>""").
            append(result.entry.runner.category.getShortName()).
            append("""
                    </td>
                    <td>""").
            append(result.DNF ? DNF_STRING : format(result.duration())).
            append("""
                        </td>
                    </tr>""");
        }
    }

    private void printOverallResultsBody(final OutputStreamWriter writer, final List<Category> result_categories) throws IOException {

        final List<RaceResult> results = race.getResultsByCategory(result_categories);

        setPositionStrings(results, true);
        printResults(results, new OverallResultPrinterHTML(writer));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    record OverallResultPrinterHTML(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MinitourRaceResult result = (MinitourRaceResult)r;
            final MinitourRace race = (MinitourRace) result.race;
            final List<IndividualRace> races = race.getRaces();

            writer.append("""
                    <tr>
                        <td>""").
                    append(result.completedAllRacesSoFar() ? result.position_string : "-").
                    append("""
                        </td>
                        <td>""").
                    append(race.normalisation.htmlEncode(result.runner.name)).
                    append("""
                        </td>
                        <td>""").
                    append(result.runner.category.getShortName()).
                    append("""
                        </td>
                        <td>""").
                    append(result.runner.club).
                    append("""
                        </td>""");

            for (int i = 0; i < result.times.size(); i++)
                if (result.times.get(i) != null)
                    writer.append("<td>").
                            append(format(result.times.get(i))).
                            append("</td>\n");
                else {
                    if (races.get(i) != null)
                        writer.append("<td>").
                                append("-").
                                append("</td>\n");
                }

            writer.append("""
                        <td>""").
                    append(result.completedAllRacesSoFar() ? format(result.duration()) : "-").
                    append("""
                        </td>
                    </tr>""");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("No results\n");
        }
    }

    record PrizeResultPrinterHTML(MinitourRace race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MinitourRaceResult result = (MinitourRaceResult)r;

            writer.append("<li>").
                    append(result.position_string).
                    append(" ").
                    append(race.normalisation.htmlEncode(result.runner.name)).
                    append(" (").
                    append((result.runner.club)).
                    append(") ").
                    append(format(result.duration())).
                    append("</li>\n");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("No results\n");
        }
    }
}
