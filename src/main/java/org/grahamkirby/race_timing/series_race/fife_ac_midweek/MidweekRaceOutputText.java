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
import org.grahamkirby.race_timing.common.output.OverallResultPrinterText;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.common.output.ResultPrinter;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MidweekRaceOutputText extends RaceOutputText {

    public MidweekRaceOutputText(final Race race) {
        super(race);
    }

//    @Override
//    protected void printPrizes(final OutputStreamWriter writer, final List<RaceResult> results) throws IOException {
//
//        setPositionStrings(results, true);
//        printResults(results, new ResultPrinterText(writer));
//    }

    protected ResultPrinter getPrizeResultPrinter(OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    static class PrizeResultPrinter extends OverallResultPrinterText {

        public PrizeResultPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = (MidweekRaceResult) r;

            writer.append(result.position_string).append(": ").
                    append(result.runner.name).append(" (").
                    append(result.runner.club).append(") ").
                    append(String.valueOf(result.totalScore())).append("\n");
        }
    }
}
