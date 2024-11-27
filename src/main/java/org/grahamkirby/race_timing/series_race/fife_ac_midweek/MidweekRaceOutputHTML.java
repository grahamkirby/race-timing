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
package org.grahamkirby.race_timing.series_race.fife_ac_midweek;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.stream.Collectors;

public class MidweekRaceOutputHTML extends SeriesRaceOutputHTML {

    public MidweekRaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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
                            <th>Runner</th>
                            <th>Category</th>
                            <th>Club</th>
                """);

            for (int i = 0; i < ((MidweekRace)race).getNumberOfRacesTakenPlace(); i++)
                writer.append(STR."""
                                <th>Race \{i + 1}</th>
                    """);

            writer.append("""
                        <th>Total</th>
                        <th>Completed</th>
                    </tr>
                </thead>
                <tbody>
            """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult) r);
            final MidweekRace midweek_race = (MidweekRace)race;
            final int number_of_races_taken_place = midweek_race.getNumberOfRacesTakenPlace();

            if (result.shouldBeDisplayedInResults()) {

                writer.append(STR."""
                        <tr>
                            <td>\{result.shouldDisplayPosition() ? result.position_string : ""}</td>
                            <td>\{race.normalisation.htmlEncode(result.runner.name)}</td>
                            <td>\{result.runner.category.getShortName()}</td>
                            <td>\{result.runner.club}</td>
                """);

                writer.append(
                    midweek_race.getRaces().subList(0, number_of_races_taken_place).stream().
                        map(individual_race -> STR."""
                                            <td>\{midweek_race.calculateRaceScore(individual_race, result.runner)}</td>
                            """).
                        collect(Collectors.joining())
                );

                writer.append(STR."""
                            <td>\{result.totalScore()}</td>
                            <td>\{result.completedSeries() ? "Y" : "N"}</td>
                        </tr>
                    """);
            }
        }
    }

    private static class PrizeResultPrinter extends ResultPrinterHTML {

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

            final MidweekRaceResult result = ((MidweekRaceResult)r);

            writer.append(STR."""
                    <li>\{result.position_string}: \{result.runner.name} (\{result.runner.category.getShortName()}) \{result.totalScore()}</li>
                """);
        }
    }
}
