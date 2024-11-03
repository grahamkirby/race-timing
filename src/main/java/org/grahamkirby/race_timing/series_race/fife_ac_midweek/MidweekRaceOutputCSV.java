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
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MidweekRaceOutputCSV extends SeriesRaceOutputCSV {

    public MidweekRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printResultsHeader(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeaderRootSeries(writer);
        writer.append(",Total,Completed\n");
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Prize results not printed to text file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    private static class OverallResultPrinter extends ResultPrinter {

        public OverallResultPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {
        }

        @Override
        public void printResultsFooter(final boolean include_credit_link) throws IOException {
        }

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
