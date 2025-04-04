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
package org.grahamkirby.race_timing.actual_races;

import org.grahamkirby.race_timing.RaceTest;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.series_race.tour.TourRace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class ActualRacesMinitourTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new TourRace(config_file_path);
    }

    @Test
    void minitour2023Completed1() throws IOException {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_1");
    }

    @Test
    void minitour2023Completed2() throws IOException {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_2");
    }

    @Test
    void minitour2023Completed3() throws IOException {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_3");
    }

    @Test
    void minitour2023Completed4() throws IOException {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_4");
    }

    @Test
    void minitour2023Completed5() throws IOException {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_5");
    }

    @Test
    void minitour2024() throws IOException {
        testExpectedCompletion("actual_races/series_race/minitour/2024");
    }
}
