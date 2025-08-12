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
package org.grahamkirby.race_timing;


import org.grahamkirby.race_timing.series_race.midweek.MidweekRace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.*;
import static org.grahamkirby.race_timing.common.Race.KEY_YEAR;
import static org.grahamkirby.race_timing.individual_race.TimedRaceInput.KEY_ENTRIES_PATH;
import static org.grahamkirby.race_timing.individual_race.TimedRaceInput.KEY_RAW_RESULTS_PATH;
import static org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace.KEY_QUALIFYING_CLUBS;
import static org.grahamkirby.race_timing.series_race.midweek.MidweekRace.KEY_SCORE_FOR_FIRST_PLACE;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

public class MidweekTest extends AbstractRaceTest {

    private static final List<String> TESTS_EXPECTED_TO_COMPLETE = List.of(
        "series_race/midweek/dead_heats",
        "series_race/midweek/duplicate_runner_name",
        "series_race/midweek/prize_category_groups",
        "series_race/midweek/large_race",
        "series_race/midweek/name_includes_comma"
    );

    private static final Object[][] TESTS_EXPECTED_TO_GIVE_ERROR = new Object[][] {
        new Object[] {"series_race/midweek/missing_property_score_for_first_place", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_SCORE_FOR_FIRST_PLACE}' in file '\{race_test.config_file_path.getFileName()}'"}
    };

    @Override
    protected void invokeMain(String[] args) throws Exception {
        MidweekRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @FieldSource("TESTS_EXPECTED_TO_COMPLETE")
    void expectedCompletion(final String test_directory_path) throws Exception {
        testExpectedCompletion(test_directory_path);
    }

    @ParameterizedTest
    @FieldSource("TESTS_EXPECTED_TO_GIVE_ERROR")
    void expectedError(final String test_directory_path, final Function<AbstractRaceTest, String> get_expected_error_message) throws Exception {
        testExpectedErrorMessage(test_directory_path, get_expected_error_message);
    }
}
