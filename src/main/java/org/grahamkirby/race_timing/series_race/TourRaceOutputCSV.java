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


import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.*;

class TourRaceOutputCSV {

    private final Race race;

    TourRaceOutputCSV(final Race race) {
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

            final SeriesRace race_impl = (TourRaceImpl) race.getSpecific();
            final String race_names = SeriesRaceOutputCSV.getConcatenatedRaceNames(race_impl.getRaces());

            writer.append("Pos,Runner,Club,Category," + race_names + ",Total" + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final TourRaceResult result = (TourRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            writer.append(result.getPositionString() + "," + encode(runner.name) + "," + encode(runner.club) + "," + runner.category.getShortName() + ",");

            writer.append(
                result.times.stream().
                    map(time -> renderDuration(time, "-")).
                    collect(Collectors.joining(","))
            );

            writer.append("," + renderDuration(result, "-") + LINE_SEPARATOR);
        }
    }
}
