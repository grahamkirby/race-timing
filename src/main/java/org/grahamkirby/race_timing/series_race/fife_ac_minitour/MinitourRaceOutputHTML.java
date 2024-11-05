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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.OverallResultPrinterHTML;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class MinitourRaceOutputHTML extends SeriesRaceOutputHTML {

    public MinitourRaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void printIndividualRaces() throws IOException {

        for (int i = 1; i <= ((SeriesRace)race).getRaces().size(); i++)
            printIndividualRaceResults(i);
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printIndividualRaceResults(final int race_number) throws IOException {

        final IndividualRace individual_race = ((SeriesRace)race).getRaces().get(race_number - 1);

        if (individual_race != null) {

            final OutputStream race_stream = Files.newOutputStream(output_directory_path.resolve("race" + race_number + ".html"));

            try (final OutputStreamWriter writer = new OutputStreamWriter(race_stream)) {
                printIndividualRaceResults(writer, individual_race);
            }
        }
    }

    private void printIndividualRaceResults(final OutputStreamWriter writer, final IndividualRace individual_race) throws IOException {

        for (final PrizeCategoryGroup group : race.prize_category_groups)
            printIndividualRaceResults(writer, individual_race, group.categories(), group.combined_categories_title());
    }

    private void printIndividualRaceResults(final OutputStreamWriter writer, final IndividualRace individual_race, final List<PrizeCategory> prize_categories, final String sub_heading) throws IOException {
        
        final List<RaceResult> category_results =
                individual_race.getOverallResults().
                stream().
                filter(result -> prizeCategoriesIncludesEligible(((IndividualRaceResult)result).entry.runner.category, prize_categories)).
                toList();

        printIndividualRaceResults(writer, category_results, sub_heading);
    }

    private void printIndividualRaceResults(final OutputStreamWriter writer, final List<RaceResult> individual_race_results) throws IOException {

        int position = 1;

        for (final RaceResult res : individual_race_results) {

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

    private void printIndividualRaceResults(final OutputStreamWriter writer, final List<RaceResult> individual_race_results, final String sub_heading) throws IOException {

        writer.append("<h4>").
                append(sub_heading).
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

        printIndividualRaceResults(writer, individual_race_results);

        writer.append("""
                    </tbody>
                </table>
                """);
    }

    private boolean prizeCategoriesIncludesEligible(final EntryCategory entry_category, final List<PrizeCategory> prize_categories) {

        for (final PrizeCategory category : prize_categories)
            if (race.isEligibleFor(entry_category, category)) return true;

        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static class OverallResultPrinter extends OverallResultPrinterHTML {

        public OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

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
                else
                    if (races.get(i) != null)
                        writer.append("<td>").
                                append("-").
                                append("</td>\n");

            writer.append("""
                        <td>""").
                    append(result.completedAllRacesSoFar() ? format(result.duration()) : "-").
                    append("""
                        </td>
                    </tr>""");
        }
    }

    private static class PrizeResultPrinter extends OverallResultPrinterHTML {

        public PrizeResultPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>\n");
        }

        @Override
        public void printResultsFooter(final boolean include_credit_link) throws IOException {

            writer.append("</ul>\n\n");
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MinitourRaceResult result = (MinitourRaceResult)r;

            writer.append("<li>").
                    append(result.position_string).append(" ").
                    append(race.normalisation.htmlEncode(result.runner.name)).append(" (").
                    append((result.runner.club)).append(") ").
                    append(format(result.duration())).append("</li>\n");
        }
    }
}
