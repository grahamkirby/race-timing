/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.series_race;


import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML.getPrizesHeader;

class MidweekRaceOutputHTML {

    private final Race race;

    MidweekRaceOutputHTML(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        SeriesRaceOutputHTML.printResults(race, OverallResultPrinter::new);
    }

    void printCombined() throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(SeriesRaceOutputHTML.getOutputStream(race, "combined"))) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(getPrizesHeader(race));
            SeriesRaceOutputHTML.printPrizes(race, writer, PrizeResultPrinter::new);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            final ResultPrinter printer = new OverallResultPrinter(race, writer);

            // Don't display category group headers if there is only one group.
            SeriesRaceOutputHTML.printResults(writer, printer, this::getResultsSubHeader, race);

            writer.append(Config.SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    public void printPrizes() throws IOException {

        SeriesRaceOutputHTML.printPrizes(race, PrizeResultPrinter::new);
    }

    public String getResultsSubHeader(final String s) {
        return STR."""
            <p></p>
            <h4>\{s}</h4>
            """;
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

            final List<Race> races = ((MidweekRaceImpl) race.getSpecific()).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
                    headers.add(STR."Race \{i + 1}");

            headers.add("Total");
            headers.add("Completed?");

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final MidweekRaceResult result = (MidweekRaceResult) r;

            elements.add(result.position_string);
            elements.add(race.getNormalisation().htmlEncode(result.runner.name));
            elements.add(result.runner.category.getShortName());
            elements.add(result.runner.club);

            for (final Race individual_race : ((MidweekRaceImpl) race.getSpecific()).getRaces())
                if (individual_race != null) {
                    final int score = ((MidweekRaceImpl) race.getSpecific()).calculateRaceScore(individual_race, result.runner);
                    elements.add(String.valueOf(score));
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
