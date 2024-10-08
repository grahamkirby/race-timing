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
package org.grahamkirby.race_timing.series_race.fife_ac_minitour;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class MinitourRaceOutputCSV extends RaceOutputCSV {

    public MinitourRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeaderRootSeries(writer);
        writer.append(",Total\n");
    }

    @Override
    protected ResultPrinter getResultPrinter(final OutputStreamWriter writer) {
        return new ResultPrinterCSV(writer);
    }

    @Override
    protected boolean allowEqualPositions() {

        // There can be dead heats in overall results, since these are determined by sum of multiple race times.
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MinitourRaceResult result = (MinitourRaceResult) r;

            writer.append(result.completedAllRacesSoFar() ? result.position_string : "-").append(",").
                    append(result.runner.name).append(",").
                    append(result.runner.club).append(",").
                    append(result.runner.category.getShortName()).append(",");

            for (final Duration time : result.times)
                writer.append(time != null ? format(time) : "-").append(",");

            writer.append(result.completedAllRacesSoFar() ? format(result.duration()) : "-").append("\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("No results\n");
        }
    }
}
