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
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MidweekRaceOutputCSV extends RaceOutputCSV {

    public MidweekRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeaderRootSeries(writer);
        writer.append(",Total,Completed\n");
    }

    @Override
    protected ResultPrinter getResultPrinter(final OutputStreamWriter writer) {
        return new ResultPrinterCSV(race, writer);
    }

    @Override
    protected boolean allowEqualPositions() {

        // There can be dead heats in overall results, since these are determined by sum of multiple race points.
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record ResultPrinterCSV(Race race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = (MidweekRaceResult) r;

            if (result.shouldDisplayPosition())
                writer.append(result.position_string);

            writer.append(",").
                    append(result.runner.name).
                    append(",").
                    append(result.runner.club).
                    append(",").
                    append(result.runner.category.getShortName()).
                    append(",");

            for (final int score : result.scores)
                if (score >= 0) writer.append(String.valueOf(score)).append(",");

            writer.append(String.valueOf(result.totalScore())).
                    append(",").
                    append(result.completed() ? "Y" : "N").
                    append("\n");
        }

        @Override
        public void printNoResults() {
        }
    }
}
