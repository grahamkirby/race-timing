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
import org.grahamkirby.race_timing.individual_race.TimedIndividualRace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

public class ActualRacesIndividualTest extends AbstractRaceTest {

    private static final List<String> TESTS_EXPECTED_TO_COMPLETE = List.of(
        "actual_races/individual_race/balmullo/2023",
        "actual_races/individual_race/balmullo/2024",
        "actual_races/individual_race/balmullo/2025",
        "actual_races/individual_race/ceres_8/2025",
        "actual_races/individual_race/cupar_5/2025",
        "actual_races/individual_race/dunnikier/2024",
        "actual_races/individual_race/giffordtown/2023",
        "actual_races/individual_race/giffordtown/2024",
        "actual_races/individual_race/hill_of_tarvit/2024",
        "actual_races/individual_race/hill_of_tarvit/2025",
        "actual_races/individual_race/junior_hill_races/2017",
        "actual_races/individual_race/normans_law/2025",
        "actual_races/individual_race/st_andrews/2023",
        "actual_races/individual_race/st_andrews/2024",
        "actual_races/individual_race/st_andrews/2025",
        "actual_races/individual_race/strath_blebo/2023",
        "actual_races/individual_race/strath_blebo/2024",
        "actual_races/individual_race/strath_blebo/2025"
    );

    @Override
    protected void invokeMain(String[] args) throws Exception {
        TimedIndividualRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @FieldSource("TESTS_EXPECTED_TO_COMPLETE") // six numbers
    void expectedCompletion(final String test_directory_path) throws Exception {
        testExpectedCompletion(test_directory_path);
    }
}
