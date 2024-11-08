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
import org.grahamkirby.race_timing.common.output.OverallResultPrinterHTML;
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

            writer.append("<h4>Overall</h4>\n");
            printResults(writer, false);

            writer.append("<h4>Full Results</h4>\n");
            printDetailedResults(writer, false);

            writer.append("<p>M3: mass start leg 3<br />M4: mass start leg 4</p>\n");

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

            final String group_title = group.group_title();
            final List<PrizeCategory> prize_categories = group.categories();

            final String sub_heading = race.prize_category_groups.size() == 1 ? "" : makeSubHeading(group_title);

            printDetailedResults(writer, prize_categories, sub_heading, include_credit_link && group_number++ == race.prize_category_groups.size() - 1);
        }
    }

    private void printDetailedResults(final OutputStreamWriter writer, final List<PrizeCategory> prize_categories, final String sub_heading, boolean include_credit_link) throws IOException {

        writer.append(sub_heading);

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

    private static class OverallResultPrinter extends OverallResultPrinterHTML {

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
        public void printResult(final RaceResult r) throws IOException {

            RelayRaceResult result = ((RelayRaceResult) r);

            writer.append(STR."""
                        <tr>
                            <td>\{result.dnf() ? "" : result.position_string}</td>
                            <td>\{result.entry.bib_number}</td>
                            <td>\{race.normalisation.htmlEncode(result.entry.team.name())}</td>
                            <td>\{result.entry.team.category().getLongName()}</td>
                            <td>\{result.dnf() ? DNF_STRING : format(result.duration())}</td>
                        </tr>
                """);
        }
    }

    private static class LegResultPrinter extends OverallResultPrinterHTML {

        final int leg;

        public LegResultPrinter(Race race, OutputStreamWriter writer, int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append(STR."""
                <table class="fac-table">
                    <thead>
                        <tr>
                            <th>Pos</th>
                            <th>Runner\{((RelayRace)race).paired_legs.get(leg-1) ? "s" : ""}</th>
                            <th>Time</th>
                        </tr>
                    </thead>
                    <tbody>
                """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult leg_result = (LegResult) r;

            if (!leg_result.DNF) {
                writer.append(STR."""
                            <tr>
                                <td>\{leg_result.position_string}</td>
                                <td>\{race.normalisation.htmlEncode(leg_result.entry.team.runner_names().get(leg_result.leg_number-1))}</td>
                                <td>\{format(leg_result.duration())}</td>
                            </tr>
                    """);
            }
        }
    }

    private static class LegResultDetailsPrinter extends OverallResultPrinterHTML {

        public LegResultDetailsPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            boolean any_previous_leg_dnf = false;

            for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

                final LegResult leg_result = result.leg_results.get(leg_number - 1);

                writer.append(STR."""
                                <td>\{race.normalisation.htmlEncode(leg_result.entry.team.runner_names().get(leg_number - 1))}\{((RelayRace)race).getMassStartAnnotation(leg_result, leg_number)}</td>
                                <td>\{leg_result.DNF ? DNF_STRING : format(leg_result.duration())}</td>
                                <td>\{leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : format(((RelayRace)race).sumDurationsUpToLeg(result.leg_results, leg_number))}</td>
                    """);

                if (leg_result.DNF) any_previous_leg_dnf = true;
            }
        }
    }

    private static class DetailedResultPrinter extends OverallResultPrinterHTML {

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

                writer.append(STR."""
                                <th>Runner\{((RelayRace)race).paired_legs.get(leg_number-1) ? "s" : ""} \{leg_number}</th>
                                <th>Leg \{leg_number}</th>
                                <th>\{leg_number < ((RelayRace)race).number_of_legs ? STR."Split \{leg_number}" : "Total"}</th>
                    """);
            }

            writer.append("""
                        </tr>
                    </thead>
                    <tbody>
                """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;


            writer.append(STR."""
                    <tr>
                        <td>\{result.dnf() ? "" : result.position_string}</td>
                        <td>\{result.entry.bib_number}</td>
                        <td>\{race.normalisation.htmlEncode(result.entry.team.name())}</td>
                        <td>\{result.entry.team.category().getLongName()}</td>
            """);

            leg_details_printer.printResult(result);

            writer.append("""
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

            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(STR."""
                    <li>\{result.position_string} \{race.normalisation.htmlEncode(result.entry.team.name())} (\{result.entry.team.category().getLongName()}) \{format(result.duration())}</li>
                """);
        }
    }
}
