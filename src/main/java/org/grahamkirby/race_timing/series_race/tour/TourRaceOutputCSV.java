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
package org.grahamkirby.race_timing.series_race.tour;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.output.ResultPrinterCSV;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

public class TourRaceOutputCSV extends SeriesRaceOutputCSV {

    TourRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    public String getResultsHeader() {
        return STR."\{getSeriesResultsHeader()},Total\{LINE_SEPARATOR}";
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

    private static final class OverallResultPrinter extends ResultPrinterCSV {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final TourRaceResult result = (TourRaceResult) r;

            final String position = result.shouldDisplayPosition() ? result.position_string : "-";
            final Runner runner = result.runner;

            writer.append(STR."\{position},\{encode(runner.name)},\{encode(runner.club)},\{runner.category.getShortName()},");

            for (final Duration time : result.times)
                writer.append(time != null ? format(time) : "-").append(",");

            writer.append(result.shouldDisplayPosition() ? format(result.duration()) : "-").append(LINE_SEPARATOR);
        }
    }
}
