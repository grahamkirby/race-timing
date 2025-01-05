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
package org.grahamkirby.race_timing;

import org.grahamkirby.race_timing.common.Race;
import org.junit.jupiter.api.Test;
import org.grahamkirby.race_timing.series_race.midweek.MidweekRace;

import java.io.IOException;
import java.nio.file.Path;

public class MidweekTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new MidweekRace(config_file_path);
    }

    @Test
    void deadHeats() throws IOException {
        testExpectedCompletion("series_race/midweek/dead_heats");
    }

    @Test
    void duplicateRunnerName() throws IOException {
        testExpectedCompletion("series_race/midweek/duplicate_runner_name");
    }

    @Test
    void prizeCategoryGroups() throws IOException {
        testExpectedCompletion("series_race/midweek/prize_category_groups");
    }

    @Test
    void largeRace() throws IOException {
        testExpectedCompletion("series_race/midweek/large_race");
    }

    @Test
    void nameIncludesComma() throws IOException {
        testExpectedCompletion("series_race/midweek/name_includes_comma");
    }
}
