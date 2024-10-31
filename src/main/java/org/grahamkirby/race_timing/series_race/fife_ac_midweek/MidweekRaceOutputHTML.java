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
        return new OverallResultPrinterHTML(((MidweekRace)race), writer);
    }

    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinterHTML(((MidweekRace)race), writer);
    }

    @Override
    protected void printResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>Runner</th>
                                       <th>Category</th>
                                       <th>Club</th>
            """);

        printHeadings(writer);

        writer.append("""
                                       <th>Total</th>
                                       <th>Completed</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printHeadings(final OutputStreamWriter writer) throws IOException {

        final List<IndividualRace> races = ((MidweekRace)race).getRaces();

        for (int i = 0; i < races.size(); i++) {
            if (races.get(i) != null) {
                writer.append("<th>Race ").
                        append(String.valueOf(i + 1)).
                        append("</th>\n");
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record OverallResultPrinterHTML(MidweekRace race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            MidweekRaceResult result = ((MidweekRaceResult)r);

            writer.append("""
                        <tr>
                        <td>""").
                    append(result.shouldDisplayPosition() ? result.position_string : "").
                    append("""
                            </td>
                            <td>""").
                    append(race.normalisation.htmlEncode(result.runner.name)).append("""
                            </td>
                            <td>""").
                    append(result.runner.category.getShortName()).append("""
                            </td>
                            <td>""").
                    append(result.runner.club).append("""
                            </td>
                            """);

            for (int i = 0; i < result.scores.size(); i++)
                if (result.scores.get(i) >= 0)
                    writer.append("<td>").append(String.valueOf(result.scores.get(i))).append("</td>\n");

            writer.append("""
                            <td>""").
                    append(String.valueOf(result.totalScore())).
                    append("""
                            </td>
                            <td>""").
                    append(result.completed() ? "Y" : "N").
                    append("""
                        </td>
                        </tr>
                        """);
        }

        @Override
        public void printNoResults() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private record PrizeResultPrinterHTML(MidweekRace race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult)r);

            writer.append("<li>").
                    append(result.position_string).append(": ").
                    append(result.runner.name).
                    append(" (").
                    append(result.runner.category.getShortName()).
                    append(") ").
                    append(String.valueOf(result.totalScore())).
                    append("</li>\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("No results\n");
        }
    }
}
