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

    // TODO rewrite with result printer
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

        setPositionStrings(category_results, false);
        printIndividualRaceResults(writer, category_results, sub_heading);
    }

    private void printIndividualRaceResults(final OutputStreamWriter writer, final List<RaceResult> individual_race_results) throws IOException {

        for (final RaceResult res : individual_race_results) {

            final IndividualRaceResult result = (IndividualRaceResult) res;

            writer.append(STR."""
                    <tr>
                        <td>\{result.DNF ? "" : result.position_string}</td>
                        <td>\{result.entry.bib_number}</td>
                        <td>\{race.normalisation.htmlEncode(result.entry.runner.name)}</td>
                        <td>\{result.entry.runner.category.getShortName()}</td>
                        <td>\{result.DNF ? DNF_STRING : format(result.duration())}</td>
                    </tr>
            """);
        }
    }

    private void printIndividualRaceResults(final OutputStreamWriter writer, final List<RaceResult> individual_race_results, final String sub_heading) throws IOException {

        writer.append(STR."""

                <h4>\{sub_heading}</h4>
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
                    writer.append(STR."            <th>Race \{String.valueOf(i + 1)}</th>\n");

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

            writer.append(STR."""
                        <tr>
                            <td>\{result.completedAllRacesSoFar() ? result.position_string : "-"}</td>
                            <td>\{race.normalisation.htmlEncode(result.runner.name)}</td>
                            <td>\{result.runner.category.getShortName()}</td>
                            <td>\{result.runner.club}</td>
                """);

            for (int i = 0; i < result.times.size(); i++)
                if (result.times.get(i) != null)
                    writer.append(STR."            <td>\{format(result.times.get(i))}</td>\n");
                else
                    if (races.get(i) != null)
                        writer.append("            <td>-</td>\n");

            writer.append(STR."""
                            <td>\{result.completedAllRacesSoFar() ? format(result.duration()) : "-"}</td>
                        </tr>
                """);
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

            writer.append(STR."    <li>\{result.position_string} \{race.normalisation.htmlEncode(result.runner.name)} (\{result.runner.club}) \{format(result.duration())}</li>\n");
        }
    }
}
