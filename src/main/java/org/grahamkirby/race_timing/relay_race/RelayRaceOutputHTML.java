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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.ResultPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class RelayRaceOutputHTML extends RaceOutputHTML {

    private String detailed_results_filename;

    public RelayRaceOutputHTML(final RelayRace race) {

        super(race);
        constructFilePaths();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void printDetailedResults(final boolean include_credit_link) throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(detailed_results_filename + ".html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printDetailedResults(writer, include_credit_link);
        }
    }

    @Override
    public void printCombined() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3><strong>Results</strong></h3>\n");
            printPrizes(writer);

            writer.append("""
                    <h4>Overall</h4>
                    """);

            printResults(writer, false);

            writer.append("""
                    <h4>Full Results</h4>
                    """);

            printDetailedResults(writer, false);

            writer.append("""
                    <p>M3: mass start leg 3<br />M4: mass start leg 4</p>
                    """);

            for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

                writer.append("<p></p>\n<h4>Leg ").append(String.valueOf(leg_number)).append(" Results</h4>\n");
                printLegResults(writer, leg_number, leg_number == ((RelayRace)race).number_of_legs);
            }
        }
    }

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    protected void printDetailedResults(final OutputStreamWriter writer, final boolean include_credit_link) throws IOException {

        int group_number = 0;
        for (final PrizeCategoryGroup group : race.prize_category_groups) {

            final String group_title = group.combined_categories_title();
            final List<PrizeCategory> prize_categories = group.categories();

            printDetailedResults(writer, prize_categories, group_title, race.prize_category_groups.size() > 1, include_credit_link && group_number++ == race.prize_category_groups.size() - 1);
        }
    }

    private void printDetailedResults(final OutputStreamWriter writer, final List<PrizeCategory> prize_categories, final String sub_heading, boolean include_sub_heading, boolean include_credit_link) throws IOException {

        if (include_sub_heading) writer.append("<h4>").append(sub_heading).append("</h4>\n");

        final List<RaceResult> results = race.getOverallResultsByCategory(prize_categories);

        setPositionStrings(results, race.allowEqualPositions());
        new DetailedResultPrinter(race, writer, new LegResultDetailsPrinter(race, writer)).print(results, include_credit_link);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void printLegResults(final boolean include_credit_link) throws IOException {

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++)
            printLegResults(leg, include_credit_link);
    }

    private void printLegResults(final int leg, final boolean include_credit_link) throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printLegResults(writer, leg, include_credit_link);
        }
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg, boolean include_credit_link) throws IOException {

        final List<LegResult> leg_results = ((RelayRace) race).getLegResults(leg);
        final List<RaceResult> results = leg_results.stream().map(result -> (RaceResult)result).toList();

        new LegResultPrinter(race, writer, leg).print(results, include_credit_link);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static class OverallResultPrinter extends ResultPrinter {

        public OverallResultPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("""
            <table class="fac-table">
                           <thead>
                               <tr>
                                   <th>Pos</th>
                                   <th>No</th>
                                   <th>Team</th>
                                   <th>Category</th>
                                   <th>Total</th>
                               </tr>
                           </thead>
                           <tbody>
            """);
        }

        @Override
        public void printResultsFooter(final boolean include_credit_link) throws IOException {

            writer.append("""
                </tbody>
            </table>
            """);

            if (include_credit_link) writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }

        @Override
        public void print(List<RaceResult> results, boolean include_credit_link) throws IOException {

            printResultsHeader();

            for (final RaceResult result : results)
                printResult(result);

            if (results.isEmpty())
                printNoResults();

            printResultsFooter(include_credit_link);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            RelayRaceResult result = ((RelayRaceResult) r);

            writer.append("""
                        <tr>
                            <td>""");
            if (!result.dnf()) writer.append(result.position_string);
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.entry.bib_number));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(race.normalisation.htmlEncode(result.entry.team.name()));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.entry.team.category().getLongName());
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.dnf() ? DNF_STRING : format(result.duration()));
            writer.append("""
                            </td>
                        </tr>""");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("<p>No results</p>\n");
        }
    }

    private static class LegResultPrinter extends ResultPrinter {

        final int leg;

        public LegResultPrinter(Race race, OutputStreamWriter writer, int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("""
            <table class="fac-table">
                <thead>
                    <tr>
                        <th>Pos</th>
                        <th>Runner""");

            if (((RelayRace)race).paired_legs.get(leg-1)) writer.append("s");

            writer.append("""
            </th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
            """);

        }

        @Override
        public void printResultsFooter(final boolean include_credit_link) throws IOException {

            writer.append("""
                </tbody>
            </table>
            """);

            if (include_credit_link) writer.append(SOFTWARE_CREDIT_LINK_TEXT);

        }

        @Override
        public void print(List<RaceResult> results, boolean include_credit_link) throws IOException {

            if (!results.isEmpty()) {
                printResultsHeader();
                for (final RaceResult result : results)
                    printResult(result);
                printResultsFooter(include_credit_link);
            }
            else
                printNoResults();
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult leg_result = (LegResult) r;

            if (!leg_result.DNF) {
                writer.append("""
                                <tr>
                                <td>""");
                writer.append(leg_result.position_string);
                writer.append("""
                                </td>
                                <td>""");
                writer.append(race.normalisation.htmlEncode(leg_result.entry.team.runner_names().get(leg_result.leg_number-1)));
                writer.append("""
                                </td>
                                <td>""");
                writer.append(format(leg_result.duration()));
                writer.append("""
                                </td>
                            </tr>""");
            }
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("<p>No results</p>\n");
        }
    }

    private static class LegResultDetailsPrinter extends ResultPrinter {

        public LegResultDetailsPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            throw new UnsupportedOperationException();

        }

        @Override
        public void printResultsFooter(final boolean include_credit_link) throws IOException {

            throw new UnsupportedOperationException();

        }

        @Override
        public void print(List<RaceResult> results, boolean include_credit_link) throws IOException {

            throw new UnsupportedOperationException();
        }
        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            boolean any_previous_leg_dnf = false;

            for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

                final LegResult leg_result = result.leg_results.get(leg_number - 1);

                writer.append("""
                <td>""");
                writer.append(race.normalisation.htmlEncode(leg_result.entry.team.runner_names().get(leg_number - 1)));

                ((RelayRace)race).addMassStartAnnotation(writer, leg_result, leg_number);

                writer.append("""
                </td>
                <td>""");

                writer.append(leg_result.DNF ? DNF_STRING : format(leg_result.duration()));
                writer.append("""
                </td>
                <td>""");
                writer.append(leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : format(((RelayRace)race).sumDurationsUpToLeg(result.leg_results, leg_number)));
                writer.append("""
                </td>""");

                if (leg_result.DNF) any_previous_leg_dnf = true;
            }
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("<p>No results</p>\n");
        }
    }

    private static class DetailedResultPrinter extends ResultPrinter {

        private final ResultPrinter leg_details_printer;

        public DetailedResultPrinter(Race race, OutputStreamWriter writer, ResultPrinter leg_details_printer) {
            super(race, writer);
            this.leg_details_printer = leg_details_printer;
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>No</th>
                                       <th>Team</th>
                                       <th>Category</th>
            """);

            for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

                writer.append("<th>Runner");
                if (((RelayRace)race).paired_legs.get(leg_number-1)) writer.append("s");
                writer.append(" ").append(String.valueOf(leg_number)).append("</th>");

                writer.append("<th>Leg ").append(String.valueOf(leg_number)).append("</th>");

                if (leg_number < ((RelayRace)race).number_of_legs)
                    writer.append("<th>Split ").append(String.valueOf(leg_number)).append("</th>");
                else
                    writer.append("<th>Total</th>");
            }

            writer.append("""
                                   </tr>
                               </thead>
                               <tbody>
            """);
        }

        @Override
        public void printResultsFooter(final boolean include_credit_link) throws IOException {

            writer.append("""
                </tbody>
            </table>
            """);

            if (include_credit_link) writer.append(SOFTWARE_CREDIT_LINK_TEXT);

        }

        @Override
        public void print(List<RaceResult> results, boolean include_credit_link) throws IOException {

            printResultsHeader();

            for (final RaceResult result : results)
                printResult(result);

            if (results.isEmpty())
                printNoResults();

            printResultsFooter(include_credit_link);
        }
        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append("""
                <tr>
                <td>""");
            if (!result.dnf()) writer.append(result.position_string);
            writer.append("""
                </td>
                <td>""");
            writer.append(String.valueOf(result.entry.bib_number));
            writer.append("""
                </td>
                <td>""");
            writer.append(race.normalisation.htmlEncode(result.entry.team.name()));
            writer.append("""
                </td>
                <td>""");
            writer.append(result.entry.team.category().getLongName());
            writer.append("""
                </td>""");

            leg_details_printer.printResult(result);

            writer.append("""
                </tr>""");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("<p>No results</p>\n");
        }
    }

    private static class PrizeResultPrinter extends ResultPrinter {

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

            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append("<li>").
                    append(result.position_string).append(" ").
                    append(race.normalisation.htmlEncode(result.entry.team.name())).append(" (").
                    append(result.entry.team.category().getLongName()).append(") ").
                    append(format(result.duration())).append("</li>\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("<p>No results</p>\n");
        }
    }
}

