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
package org.grahamkirby.race_timing.series_race.grand_prix;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_RESULTS;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

class GrandPrixRaceOutputCSV extends SeriesRaceOutputCSV {

    GrandPrixRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    public final String getResultsHeader() {

        return STR."Pos,Runner,Category,\{((SeriesRace) race).getRaces().stream().
            filter(Objects::nonNull).
            map(race1 -> race1.getRequiredProperty(KEY_RACE_NAME_FOR_RESULTS)).
            collect(Collectors.joining(","))}" + STR.",Total,Completed?\{getRaceCategoriesHeader()}\n";
    }

    @Override
    protected final ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    // Prize results not printed to text file.
    @Override
    protected final ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }

    private String getRaceCategoriesHeader() {

        return ((GrandPrixRace) race).race_categories.stream().
            map(category -> STR.",\{category.category_title()}?").
            collect(Collectors.joining());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = ((GrandPrixRaceResult) r);
            final GrandPrixRace grand_prix_race = (GrandPrixRace) race;

            writer.append(STR."\{result.position_string},\{encode(result.runner.name)},\{result.runner.category.getShortName()},");

            writer.append(
                grand_prix_race.getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> renderScore(grand_prix_race.calculateRaceScore(individual_race, result.runner), "-")).
                    collect(Collectors.joining(","))
            );

            writer.append(STR.",\{result.totalScore()},\{result.hasCompletedSeries() ? "Y" : "N"}");

            for (final RaceCategory category : ((GrandPrixRace) race).race_categories)
                writer.append(",").append(result.hasCompletedRaceCategory(category) ? "Y" : "N");

            writer.append(LINE_SEPARATOR);
        }
    }
}
