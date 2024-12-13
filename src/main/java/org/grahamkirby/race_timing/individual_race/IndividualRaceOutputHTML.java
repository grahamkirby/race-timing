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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.CreditLink;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.ResultPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class IndividualRaceOutputHTML extends RaceOutputHTML {

    public IndividualRaceOutputHTML(final IndividualRace race) {
        super(race);
    }

    @Override
    public void printCombined() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3><strong>Results</strong></h3>\n");
            printPrizes(writer);
            writer.append("<h4>Overall</h4>\n");
            printResults(writer, getOverallResultPrinter(writer), CreditLink.INCLUDE_CREDIT_LINK);
        }
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

    private static class OverallResultPrinter extends ResultPrinterHTML {

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
                            <th>No</th>
                            <th>Runner</th>
                            <th>Club</th>
                            <th>Cat</th>
                            <th>Time</th>
                        </tr>
                    </thead>
                    <tbody>
                """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final IndividualRaceResult result = ((IndividualRaceResult)r);

            writer.append(STR."""
                    <tr>
                        <td>\{result.shouldDisplayPosition() ? result.position_string : ""}</td>
                        <td>\{result.entry.bib_number}</td>
                        <td>\{race.normalisation.htmlEncode(result.entry.runner.name)}</td>
                        <td>\{result.entry.runner.club}</td>
                        <td>\{result.entry.runner.category.getShortName()}</td>
                        <td>\{result.getCompletionStatus() != CompletionStatus.COMPLETED ? DNF_STRING : format(result.duration())}</td>
                    </tr>
                """);
        }
    }

    private static class PrizeResultPrinter extends ResultPrinterHTML {

        public PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>\n");
        }

        @Override
        public void printResultsFooter(final CreditLink credit_link_option) throws IOException {

            writer.append("</ul>\n\n");
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final IndividualRaceResult result = ((IndividualRaceResult)r);

            writer.append(STR."    <li>\{result.position_string} \{race.normalisation.htmlEncode(result.entry.runner.name)} (\{result.entry.runner.club}) \{format(result.duration())}</li>\n");
        }
    }
}
