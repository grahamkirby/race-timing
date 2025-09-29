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
import org.grahamkirby.race_timing.individual_race.Runner;

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

        SeriesRaceOutputCSV.printResults(race, OverallResultPrinter::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final GrandPrixRaceImpl race_impl = (GrandPrixRaceImpl) race.getSpecific();
            final String race_names = getConcatenatedRaceNames(race_impl.getRaces());

            final String race_categories_header = race_impl.race_categories.stream().
                map(GrandPrixRaceCategory::category_title).
                collect(Collectors.joining("?,")) + "?";

            writer.append("Pos,Runner,Category," + race_names + ",Total,Completed," + race_categories_header + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = (GrandPrixRaceResult) r;
            final GrandPrixRaceImpl race_impl = (GrandPrixRaceImpl) race.getSpecific();
            final GrandPrixRaceResultsCalculatorImpl calculator = (GrandPrixRaceResultsCalculatorImpl) race.getResultsCalculator();
            final Runner runner = (Runner) result.getParticipant();

            writer.append(result.getPositionString() + "," + encode(runner.name) + "," + runner.category.getShortName() + ",");

            writer.append(
                race_impl.getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> calculator.calculateRaceScore(individual_race, runner)).
                    map(OverallResultPrinter::renderScore).
                    collect(Collectors.joining(","))
            );

            writer.append("," + result.totalScore() + "," + (result.hasCompletedSeries() ? "Y" : "N") + ",");

            writer.append(
                race_impl.race_categories.stream().
                    map(category -> result.hasCompletedRaceCategory(category) ? "Y" : "N").
                    collect(Collectors.joining(","))
            );

            writer.append(LINE_SEPARATOR);
        }

        private static String renderScore(final int score) {

            return score != 0 ? String.valueOf(score) : "-";
        }
    }
}
