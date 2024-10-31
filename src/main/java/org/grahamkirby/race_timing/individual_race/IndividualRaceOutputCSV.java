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
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class IndividualRaceOutputCSV extends RaceOutputCSV {

    public static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time";

    public IndividualRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printResultsHeader(final OutputStreamWriter writer) throws IOException {
        writer.append(OVERALL_RESULTS_HEADER).append("\n");
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new ResultPrinterCSV(writer);
    }

    // Prize results not printed to text file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final IndividualRaceResult result = (IndividualRaceResult)r;

            if (!result.dnf())
                writer.append(String.valueOf(result.position_string));

            writer.append(",").
                    append(String.valueOf(result.entry.bib_number)).append(",").
                    append(result.entry.runner.name).append(",").
                    append((result.entry.runner.club)).append(",").
                    append(result.entry.runner.category.getShortName()).append(",").
                    append(result.dnf() ? "DNF" : format(result.duration())).append("\n");
        }

        @Override
        public void printNoResults() {
        }
    }
}
