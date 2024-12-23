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
package org.grahamkirby.race_timing.actual_races;

import org.grahamkirby.race_timing.RaceTest;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class ActualRacesGrandPrixTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new GrandPrixRace(config_file_path);
    }

    @Test
    void grandPrix2016Completed6() throws IOException {
        testExpectedCompletion("actual_races/series_race/grand_prix/2016/completed_6");
    }

    @Test
    void grandPrix2016Completed10() throws IOException {
        testExpectedCompletion("actual_races/series_race/grand_prix/2016/completed_10");
    }

    @Test
    void grandPrix2016Completed12() throws IOException {
        testExpectedCompletion("actual_races/series_race/grand_prix/2016/completed_12");
    }
}
