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

import org.grahamkirby.race_timing.AbstractRaceTest;
import org.grahamkirby.race_timing.series_race.midweek.MidweekRace;
import org.junit.jupiter.api.Test;

public class ActualRacesMidweekTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(String[] args) throws Exception {
        MidweekRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void midweek2023Completed1() throws Exception {
        testExpectedCompletion("actual_races/series_race/midweek/2023/completed_1");
    }

    @Test
    void midweek2023Completed2() throws Exception {
        testExpectedCompletion("actual_races/series_race/midweek/2023/completed_2");
    }

    @Test
    void midweek2023Completed3() throws Exception {
        testExpectedCompletion("actual_races/series_race/midweek/2023/completed_3");
    }

    @Test
    void midweek2023Completed4() throws Exception {
        testExpectedCompletion("actual_races/series_race/midweek/2023/completed_4");
    }

    @Test
    void midweek2023Completed5() throws Exception {
        testExpectedCompletion("actual_races/series_race/midweek/2023/completed_5");
    }

    @Test
    void midweek2024() throws Exception {
        testExpectedCompletion("actual_races/series_race/midweek/2024");
    }
}
