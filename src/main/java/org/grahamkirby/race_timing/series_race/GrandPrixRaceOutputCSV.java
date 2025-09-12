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


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.LINE_SEPARATOR;
import static org.grahamkirby.race_timing.common.Config.encode;
import static org.grahamkirby.race_timing.series_race.SeriesRaceOutputCSV.getConcatenatedRaceNames;

class GrandPrixRaceOutputCSV {

    private final Race race;

    GrandPrixRaceOutputCSV(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printResults() throws IOException {

        SeriesRaceOutputCSV.printResults(getResultsHeader(), race, OverallResultPrinter::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private String getResultsHeader() {

        final GrandPrixRaceImpl race_impl = (GrandPrixRaceImpl) race.getSpecific();

        final String race_names = getConcatenatedRaceNames(race_impl.getRaces());

        final String race_categories_header = race_impl.race_categories.stream().
            map(category -> STR.",\{category.category_title()}?").
            collect(Collectors.joining());

        return STR."Pos,Runner,Category,\{race_names }" + STR.",Total,Completed?\{race_categories_header }\{LINE_SEPARATOR}";
    }

    private static final class OverallResultPrinter extends ResultPrinter {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = (GrandPrixRaceResult) r;
            final GrandPrixRaceImpl race_impl = (GrandPrixRaceImpl) race.getSpecific();
            final GrandPrixRaceResultsCalculatorImpl calculator = (GrandPrixRaceResultsCalculatorImpl) race.getResultsCalculator();

            writer.append(STR."\{result.position_string},\{encode(result.runner.name)},\{result.runner.category.getShortName()},");

            writer.append(
                race_impl.getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> renderScore(calculator.calculateRaceScore(individual_race, result.runner))).
                    collect(Collectors.joining(","))
            );

            writer.append(STR.",\{result.totalScore()},\{result.hasCompletedSeries() ? "Y" : "N"}");

            for (final GrandPrixRaceCategory category : race_impl.race_categories)
                writer.append(",").append(result.hasCompletedRaceCategory(category) ? "Y" : "N");

            writer.append(LINE_SEPARATOR);
        }

        private static String renderScore(final int score) {

            return score != 0 ? String.valueOf(score) : "-";
        }
    }
}
