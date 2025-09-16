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
import java.time.Duration;

import static org.grahamkirby.race_timing.common.Config.*;

class TourRaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    private final Race race;

    TourRaceOutputCSV(final Race race) {
        this.race = race;
    }

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

            final SeriesRaceImpl race_impl = (TourRaceImpl) race.getSpecific();
            final String race_names = SeriesRaceOutputCSV.getConcatenatedRaceNames(race_impl.getRaces());

            writer.append(STR."Pos,Runner,Club,Category,\{race_names},Total\{LINE_SEPARATOR}");
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final TourRaceResult result = ((TourRaceResult) r);

            writer.append(STR."\{result.position_string},\{encode(result.runner.name)},\{encode(result.runner.club)},\{result.runner.category.getShortName()},");

            for (final Duration time : result.times)
                writer.append(renderDuration(time, "-")).append(",");

            writer.append(renderDuration(result.duration(), "-")).append(LINE_SEPARATOR);
        }
    }
}
