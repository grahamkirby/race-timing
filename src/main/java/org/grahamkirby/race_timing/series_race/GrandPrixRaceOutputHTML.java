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
import java.util.function.BiFunction;

import static org.grahamkirby.race_timing.common.Config.*;

class GrandPrixRaceOutputHTML {

    private final Race race;

    GrandPrixRaceOutputHTML(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        SeriesRaceOutputHTML.printResults(race, OverallResultPrinter::new);
    }

    void printCombined() throws IOException {

        SeriesRaceOutputHTML.printCombined(race, OverallResultPrinter::new, PrizeResultPrinter::new);
    }

    public void printPrizes() throws IOException {

        SeriesRaceOutputHTML.printPrizes(race, PrizeResultPrinter::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");

            final List<String> headers = new ArrayList<>(common_headers);

            // This traverses races in order of listing in config, sorted first by race type and then date.
            for (final Race individual_race : ((GrandPrixRaceImpl) race.getSpecific()).getRaces())
                // Check whether race has taken place at this point.
                if (individual_race != null)
                    headers.add((String) individual_race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS));

            headers.add("Total");
            headers.add("Completed?");

            for (final GrandPrixRaceCategory category : ((GrandPrixRaceImpl) race.getSpecific()).race_categories)
                headers.add(STR."\{category.category_title()}?");

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final GrandPrixRaceResult result = (GrandPrixRaceResult) r;

            elements.add(result.position_string);
            elements.add(race.getNormalisation().htmlEncode(result.runner.name));
            elements.add(result.runner.category.getShortName());

            for (final Race individual_race : ((GrandPrixRaceImpl) race.getSpecific()).getRaces())
                if (individual_race != null) {
                    final int score = ((GrandPrixRaceResultsCalculatorImpl) race.getResultsCalculator()).calculateRaceScore(individual_race, result.runner);
                    elements.add(renderScore(score));
                }

            elements.add(String.valueOf(result.totalScore()));
            elements.add(result.hasCompletedSeries() ? "Y" : "N");

            for (final GrandPrixRaceCategory category : ((GrandPrixRaceImpl) race.getSpecific()).race_categories)
                elements.add(result.hasCompletedRaceCategory(category) ? "Y" : "N");

            return elements;
        }

        private static String renderScore(final int score) {

            return score != 0 ? String.valueOf(score) : "-";
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

            final GrandPrixRaceResult result = ((GrandPrixRaceResult) r);

            writer.append(STR."""
                    <li>\{result.position_string}: \{race.getNormalisation().htmlEncode(result.runner.name)} (\{result.runner.category.getShortName()}) \{result.totalScore()}</li>
                """);
        }
    }
}
