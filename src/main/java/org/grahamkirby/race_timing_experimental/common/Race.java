/*
 * Copyright 2025 Graham Kirby:
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
package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RacePrizes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public interface Race {

    void setPrizes(RacePrizes prizes);

    void setInput(RaceInput input);

    void setResultsCalculator(ResultsCalculator results_calculator);

    void setResultsOutput(ResultsOutput results_output);

    void processResults() throws IOException;

    Properties getProperties();

    Path getPath(String results_path);
}
