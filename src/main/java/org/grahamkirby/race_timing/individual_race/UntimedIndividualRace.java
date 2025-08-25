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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class UntimedIndividualRace extends SingleRace {

    public UntimedIndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    @Override
    public void calculateResults() {
    }

    @Override
    protected void recordDNF(final String dnf_specification) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected RaceInput getInput() {
        return new UntimedIndividualRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return null;
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return null;
    }

    @Override
    protected RaceOutputText getOutputText() {
        return null;
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return null;
    }

    @Override
    protected void configureInputData() throws IOException {

        ((SingleRaceInput)input).validateInputFiles();

        overall_results = ((UntimedIndividualRaceInput) input).loadOverallResults();
    }

    @Override
    protected void outputResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {
        return List.of();
    }
}
