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

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.CreditLink;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

public class RelayRaceOutputHTML extends RaceOutputHTML {

    private String detailed_results_filename;

    RelayRaceOutputHTML(final RelayRace race) {

        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printCombined() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3><strong>Results</strong></h3>").append(LINE_SEPARATOR);
            printPrizes(writer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            printResults(writer, getOverallResultPrinter(writer), CreditLink.DO_NOT_INCLUDE_CREDIT_LINK);

            writer.append("<h4>Full Results</h4>").append(LINE_SEPARATOR);
            printDetailedResults(writer, CreditLink.DO_NOT_INCLUDE_CREDIT_LINK);

            writer.append("<p>M3: mass start leg 3<br />M4: mass start leg 4</p>").append(LINE_SEPARATOR);

            for (int leg_number = 1; leg_number <= ((RelayRace) race).getNumberOfLegs(); leg_number++) {

                writer.append(STR."<p></p>\{LINE_SEPARATOR}<h4>Leg \{leg_number} Results</h4>\{LINE_SEPARATOR}");
                printLegResults(writer, leg_number, (leg_number == ((RelayRace) race).getNumberOfLegs()) ? CreditLink.INCLUDE_CREDIT_LINK : CreditLink.DO_NOT_INCLUDE_CREDIT_LINK);
            }
        }
    }

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();
        detailed_results_filename = STR."\{race_name_for_filenames}_detailed_\{year}";
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

    void printDetailedResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(STR."\{detailed_results_filename}.html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printDetailedResults(writer, CreditLink.INCLUDE_CREDIT_LINK);
        }
    }

    private void printDetailedResults(final OutputStreamWriter writer, final CreditLink credit_link_option) throws IOException {

        printResults(writer, new DetailedResultPrinter(race, writer), credit_link_option);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace) race).getNumberOfLegs(); leg++)
            printLegResults(leg);
    }

    private void printLegResults(final int leg) throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(STR."\{race_name_for_filenames}_leg_\{leg}_\{year}.html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printLegResults(writer, leg, CreditLink.INCLUDE_CREDIT_LINK);
        }
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg, final CreditLink credit_link_option) throws IOException {

        final List<LegResult> leg_results = ((RelayRace) race).getLegResults(leg);

        new LegResultPrinter(race, writer, leg).print(leg_results, credit_link_option);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
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

            final RelayRaceResult result = ((RelayRaceResult) r);

            writer.append(STR."""
                        <tr>
                            <td>\{result.getCompletionStatus() != CompletionStatus.COMPLETED ? "" : result.position_string}</td>
                            <td>\{result.entry.bib_number}</td>
                            <td>\{race.normalisation.htmlEncode(result.entry.team.name())}</td>
                            <td>\{result.entry.team.category().getLongName()}</td>
                            <td>\{result.getCompletionStatus() != CompletionStatus.COMPLETED ? DNF_STRING : format(result.duration())}</td>
                        </tr>
                """);
        }
    }

    private static final class LegResultPrinter extends ResultPrinterHTML {

        final int leg;

        private LegResultPrinter(final Race race, final OutputStreamWriter writer, final int leg) {

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
                            <th>Runner\{((RelayRace) race).getPairedLegs().contains(leg) ? "s" : ""}</th>
                            <th>Time</th>
                        </tr>
                    </thead>
                    <tbody>
                """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult leg_result = (LegResult) r;

            writer.append(STR."""
                        <tr>
                            <td>\{leg_result.position_string}</td>
                            <td>\{race.normalisation.htmlEncode(leg_result.entry.team.runner_names().get(leg_result.leg_number - 1))}</td>
                            <td>\{format(leg_result.duration())}</td>
                        </tr>
                """);
        }
    }

    private static final class DetailedResultPrinter extends ResultPrinterHTML {

        private DetailedResultPrinter(final Race race, final OutputStreamWriter writer) {
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
                """);

            for (int leg_number = 1; leg_number <= ((RelayRace) race).getNumberOfLegs(); leg_number++) {

                writer.append(STR."""
                                <th>Runner\{((RelayRace) race).getPairedLegs().contains(leg_number) ? "s" : ""} \{leg_number}</th>
                                <th>Leg \{leg_number}</th>
                                <th>\{leg_number < ((RelayRace) race).getNumberOfLegs() ? STR."Split \{leg_number}" : "Total"}</th>
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

            final RelayRace relay_race = (RelayRace) race;
            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(STR."""
                <tr>
                    <td>\{result.shouldDisplayPosition() ? result.position_string : ""}</td>
                    <td>\{result.entry.bib_number}</td>
                    <td>\{race.normalisation.htmlEncode(result.entry.team.name())}</td>
                    <td>\{result.entry.team.category().getLongName()}</td>
            """);

            boolean all_previous_legs_completed = true;

            for (int leg = 1; leg <= relay_race.getNumberOfLegs(); leg++) {

                final LegResult leg_result = result.leg_results.get(leg - 1);
                final boolean completed = leg_result.getCompletionStatus() == CompletionStatus.COMPLETED;

                final String leg_runner_names = leg_result.entry.team.runner_names().get(leg - 1);
                final String leg_mass_start_annotation = relay_race.getMassStartAnnotation(leg_result, leg);
                final String leg_time = completed ? format(leg_result.duration()) : DNF_STRING;
                final String split_time = completed && all_previous_legs_completed ? format(RelayRace.sumDurationsUpToLeg(result.leg_results, leg)) : DNF_STRING;

                writer.append(STR."""
                                <td>\{race.normalisation.htmlEncode(leg_runner_names)}\{leg_mass_start_annotation}</td>
                                <td>\{leg_time}</td>
                                <td>\{split_time}</td>
                    """);

                if (!completed) all_previous_legs_completed = false;
            }

            writer.append("""
                        </tr>
                """);
        }
    }

    private static final class PrizeResultPrinter extends ResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(LINE_SEPARATOR);
        }

        @Override
        public void printResultsFooter(final CreditLink credit_link_option) throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
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
