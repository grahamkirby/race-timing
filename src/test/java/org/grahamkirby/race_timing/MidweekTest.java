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
package org.grahamkirby.race_timing;

import org.grahamkirby.race_timing.common.Race;
import org.junit.jupiter.api.Test;
import org.grahamkirby.race_timing.series_race.fife_ac_midweek.MidweekRace;

import java.io.IOException;
import java.nio.file.Path;

public class MidweekTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new MidweekRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "series_race/";
    }

    @Test
    public void completed_1() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_1");
    }

    @Test
    public void completed_2() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_2");
    }

    @Test
    public void completed_3() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_3");
    }

    @Test
    public void completed_4() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_4");
    }

    @Test
    public void completed_5() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_5");
    }

    @Test
    public void deadHeats() throws Exception {
        testExpectedCompletion("midweek/dead_heats");
    }

    @Test
    public void duplicateRunnerName() throws Exception {
        testExpectedCompletion("midweek/duplicate_runner_name");
    }
}
