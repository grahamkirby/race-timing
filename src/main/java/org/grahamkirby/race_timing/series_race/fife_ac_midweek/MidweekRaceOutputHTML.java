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
import org.grahamkirby.race_timing.common.output.OverallResultPrinterHTML;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

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
                            <th>Runner</th>
                            <th>Category</th>
                            <th>Club</th>
                """);

            final List<IndividualRace> races = ((MidweekRace)race).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
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

            final MidweekRaceResult result = ((MidweekRaceResult)r);

            writer.append(STR."""
                        <tr>
                            <td>\{result.shouldDisplayPosition() ? result.position_string : ""}</td>
                            <td>\{race.normalisation.htmlEncode(result.runner.name)}</td>
                            <td>\{result.runner.category.getShortName()}</td>
                            <td>\{result.runner.club}</td>
                """);

            for (int i = 0; i < result.scores.size(); i++)
                if (result.scores.get(i) >= 0)
                    writer.append(STR."""
                                    <td>\{result.scores.get(i)}</td>
                        """);

            writer.append(STR."""
                            <td>\{result.totalScore()}</td>
                            <td>\{result.completed() ? "Y" : "N"}</td>
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

            final MidweekRaceResult result = ((MidweekRaceResult)r);

            writer.append(STR."""
                    <li>\{result.position_string}: \{result.runner.name} (\{result.runner.category.getShortName()}) \{result.totalScore()}</li>
                """);
        }
    }
}
