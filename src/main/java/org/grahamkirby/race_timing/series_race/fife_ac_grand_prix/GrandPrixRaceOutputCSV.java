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
package org.grahamkirby.race_timing.series_race.fife_ac_grand_prix;

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

public class GrandPrixRaceOutputCSV extends SeriesRaceOutputCSV {

    public GrandPrixRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    public String getResultsHeader() {

        return STR."Pos,Runner,Category,\{((SeriesRace) race).getRaces().stream().
                filter(Objects::nonNull).
                map(race1 -> race1.getProperty(KEY_RACE_NAME_FOR_RESULTS)).
                collect(Collectors.joining(","))}" + STR.",Total,Qualified\{getRaceCategoriesHeader()}\n";
    }

    private String getRaceCategoriesHeader() {

        final StringBuilder builder = new StringBuilder();

        for (final RaceCategory category : ((GrandPrixRace)race).race_categories)
            builder.append(",").append(category.category_title()).append("?");

        return builder.toString();
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    // Prize results not printed to text file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static class OverallResultPrinter extends ResultPrinterCSV {

        public OverallResultPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = ((GrandPrixRaceResult) r);
            final GrandPrixRace grand_prix_race = (GrandPrixRace)race;
            final int number_of_races_taken_place = grand_prix_race.getNumberOfRacesTakenPlace();

            if (result.shouldBeDisplayedInResults()) {

                writer.append(STR."\{result.shouldDisplayPosition() ? result.position_string : ""},\{encode(result.runner.name)},\{result.runner.category.getShortName()},");

                writer.append(
                    grand_prix_race.getRaces().subList(0, number_of_races_taken_place).stream().
                        map(individual_race -> {
                            String s = String.valueOf(Math.round(grand_prix_race.calculateRaceScore(individual_race, result.runner)));
                            return s.equals("0") ? "-" : s;
                        }).
                        collect(Collectors.joining(","))
                );

                writer.append(STR.",\{result.getCompletionStatus() == CompletionStatus.COMPLETED ? Math.round(result.totalScore()) : "-"},\{result.getCompletionStatus() == CompletionStatus.COMPLETED ? "Y" : "N"}");

                for (RaceCategory category : ((GrandPrixRace)race).race_categories) {
                    writer.append(",").append(grand_prix_race.hasCompletedCategory(result, category) ? "Y" : "N");
                }

                writer.append("\n");
            }
        }
    }
}
