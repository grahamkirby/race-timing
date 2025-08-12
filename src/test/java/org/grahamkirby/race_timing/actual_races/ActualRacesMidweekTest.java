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
import org.grahamkirby.race_timing.series_race.midweek.MidweekRace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

public class ActualRacesMidweekTest extends AbstractRaceTest {

    private static final List<String> TESTS_EXPECTED_TO_COMPLETE = List.of(
        "actual_races/series_race/midweek/2023/completed_1",
        "actual_races/series_race/midweek/2023/completed_2",
        "actual_races/series_race/midweek/2023/completed_3",
        "actual_races/series_race/midweek/2023/completed_4",
        "actual_races/series_race/midweek/2023/completed_5",
        "actual_races/series_race/midweek/2024",
        "actual_races/series_race/midweek/2025"
    );

    @Override
    protected void invokeMain(String[] args) throws Exception {
        MidweekRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @FieldSource("TESTS_EXPECTED_TO_COMPLETE") // six numbers
    void expectedCompletion(final String test_directory_path) throws Exception {
        testExpectedCompletion(test_directory_path);
    }
}
