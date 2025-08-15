/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race.midweek;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.grahamkirby.race_timing_experimental.common.Config.LINE_SEPARATOR;

class MidweekRaceOutputHTML extends SeriesRaceOutputHTML {

    MidweekRaceOutputHTML(final Race race) {
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

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");

            final List<String> headers = new ArrayList<>(common_headers);

            headers.add("Club");

            final List<SingleRace> races = ((SeriesRace) race).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
                    headers.add(STR."Race \{i + 1}");

            headers.add("Total");
            headers.add("Completed?");

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final MidweekRace midweek_race = (MidweekRace) race;
            final MidweekRaceResult result = (MidweekRaceResult) r;

            elements.add(result.position_string);
            elements.add(race.normalisation.htmlEncode(result.runner.name));
            elements.add(result.runner.category.getShortName());
            elements.add(result.runner.club);

            for (final SingleRace individual_race : midweek_race.getRaces())
                if (individual_race != null) {
                    final int score = midweek_race.calculateRaceScore(individual_race, result.runner);
                    elements.add(renderScore(score));
                }

            elements.add(String.valueOf(result.totalScore()));
            elements.add(result.hasCompletedSeries() ? "Y" : "N");

            return elements;
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
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult) r);

            writer.append(STR."""
                    <li>\{result.position_string}: \{result.runner.name} (\{result.runner.category.getShortName()}) \{result.totalScore()}</li>
                """);
        }
    }
}
