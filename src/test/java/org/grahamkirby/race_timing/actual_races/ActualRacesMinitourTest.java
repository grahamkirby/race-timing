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
package org.grahamkirby.race_timing.actual_races;


import org.grahamkirby.race_timing.AbstractRaceTest;
import org.grahamkirby.race_timing.series_race.tour.TourRace;
import org.junit.jupiter.api.Test;

public class ActualRacesMinitourTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(String[] args) throws Exception {
        TourRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void minitour2023Completed1() throws Exception {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_1");
    }

    @Test
    void minitour2023Completed2() throws Exception {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_2");
    }

    @Test
    void minitour2023Completed3() throws Exception {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_3");
    }

    @Test
    void minitour2023Completed4() throws Exception {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_4");
    }

    @Test
    void minitour2023Completed5() throws Exception {
        testExpectedCompletion("actual_races/series_race/minitour/2023/completed_5");
    }

    @Test
    void minitour2024() throws Exception {
        testExpectedCompletion("actual_races/series_race/minitour/2024");
    }

    @Test
    void minitour2025() throws Exception {
        testExpectedCompletion("actual_races/series_race/minitour/2025");
    }
}
