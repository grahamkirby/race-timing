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
package org.grahamkirby.race_timing.series_race.grand_prix;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterCSV;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_RESULTS;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

public class GrandPrixRaceOutputCSV extends SeriesRaceOutputCSV {

    GrandPrixRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    public final String getResultsHeader() {

        return STR."Pos,Runner,Category,\{((SeriesRace) race).getRaces().stream().
            filter(Objects::nonNull).
            map(race1 -> race1.getProperty(KEY_RACE_NAME_FOR_RESULTS)).
            collect(Collectors.joining(","))}" + STR.",Total,Qualified\{getRaceCategoriesHeader()}\n";
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

    private static final class OverallResultPrinter extends ResultPrinterCSV {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = ((GrandPrixRaceResult) r);
            final GrandPrixRace grand_prix_race = (GrandPrixRace) race;
            final int number_of_races_taken_place = grand_prix_race.getNumberOfRacesTakenPlace();

            writer.append(STR."\{result.shouldDisplayPosition() ? result.position_string : ""},\{encode(result.runner.name)},\{result.runner.category.getShortName()},");

            writer.append(
                grand_prix_race.getRaces().subList(0, number_of_races_taken_place).stream().
                    map(individual_race -> {
                        final long score = Math.round(GrandPrixRace.calculateRaceScore(individual_race, result.runner));
                        return score == 0 ? "-" : String.valueOf(score);
                    }).
                    collect(Collectors.joining(","))
            );

            writer.append(STR.",\{result.getCompletionStatus() == CompletionStatus.COMPLETED ? Math.round(result.totalScore()) : "-"},\{result.getCompletionStatus() == CompletionStatus.COMPLETED ? "Y" : "N"}");

            for (final RaceCategory category : ((GrandPrixRace) race).race_categories) {
                writer.append(",").append(GrandPrixRaceResult.hasCompletedRaceCategory(result, category) ? "Y" : "N");
            }

            writer.append(LINE_SEPARATOR);
        }
    }
}
