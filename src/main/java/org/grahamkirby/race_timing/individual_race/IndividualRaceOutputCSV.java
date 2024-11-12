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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.OverallResultPrinterCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.ResultPrinter;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class IndividualRaceOutputCSV extends RaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time\n";

    public IndividualRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    public String getResultsHeader() {
        return OVERALL_RESULTS_HEADER;
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    // Prize results not printed to CSV file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static class OverallResultPrinter extends OverallResultPrinterCSV {

        public OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final IndividualRaceResult result = (IndividualRaceResult)r;

            writer.append(STR."\{result.shouldDisplayPosition() ? result.position_string : ""}, \{result.entry.bib_number}, \{result.entry.runner.name}, ").
                    append(STR."\{result.entry.runner.club}, \{result.entry.runner.category.getShortName()}, \{!result.completed() ? "DNF" : format(result.duration())}");
        }
    }
}
