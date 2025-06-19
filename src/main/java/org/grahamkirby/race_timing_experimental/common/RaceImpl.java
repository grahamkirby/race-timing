/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.common;


import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing_experimental.individual_race.RaceInput;
import org.grahamkirby.race_timing_experimental.individual_race.SingleRaceType;

import java.io.IOException;
import java.util.List;

public interface RaceImpl {

    Race getRace();

    void setRaceInput(RaceInput race_input);

    void processProperties();

    List<RaceResult> calculateResults();

    void outputResults(List<RaceResult> results);

    void configureInputData() throws IOException;

    void setRaceType(SingleRaceType individualRace);
}
