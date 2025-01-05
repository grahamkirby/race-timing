/*
 * Copyright 2025 Graham Kirby:
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
package org.grahamkirby.race_timing.series_race.midweek;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

class MidweekRaceOutputCSV extends SeriesRaceOutputCSV {

    MidweekRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    public String getResultsHeader() {
        return STR."\{getSeriesResultsHeader()},Total,Completed\{LINE_SEPARATOR}";
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    // Prize results not printed to text file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult) r);
            final MidweekRace midweek_race = (MidweekRace) race;
            final int number_of_races_taken_place = midweek_race.getNumberOfRacesTakenPlace();

            writer.append(STR."\{result.shouldDisplayPosition() ? result.position_string : ""},\{encode(result.runner.name)},\{encode(result.runner.club)},\{result.runner.category.getShortName()},");

            writer.append(
                midweek_race.getRaces().subList(0, number_of_races_taken_place).stream().
                    map(individual_race -> String.valueOf(midweek_race.calculateRaceScore(individual_race, result.runner))).
                    collect(Collectors.joining(","))
            );

            writer.append(STR.",\{result.totalScore()},\{result.getCompletionStatus() == CompletionStatus.COMPLETED ? "Y" : "N"}\n");
        }
    }
}
