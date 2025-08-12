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

import java.util.List;

public class ActualRacesTourTest extends AbstractRaceTest {

    private static final List<String> TESTS_EXPECTED_TO_COMPLETE = List.of(
        "actual_races/series_race/grand_prix/2016/completed_6",
        "actual_races/series_race/grand_prix/2016/completed_10",
        "actual_races/series_race/grand_prix/2016/completed_12"
    );

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        TourRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void tour2017Completed3() throws Exception {
        testExpectedCompletion("actual_races/series_race/tour/2017/completed_3");
    }

    @Test
    void tour2017Completed5() throws Exception {
        testExpectedCompletion("actual_races/series_race/tour/2017/completed_5");
    }
}
