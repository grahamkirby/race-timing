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


import org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace.*;

class GrandPrixTest extends AbstractRaceTest {

    private static final List<String> TESTS_EXPECTED_TO_COMPLETE = List.of(
        "series_race/grand_prix/legal_category_change",
        "series_race/grand_prix/unknown_category"
    );

    private static final Object[][] TESTS_EXPECTED_TO_GIVE_ERROR = new Object[][] {
        new Object[] {"series_race/grand_prix/illegal_category_change_age_reduction", (Function<AbstractRaceTest, String>) _ -> "invalid category change: runner 'Fictional Runner2' changed from F40 to FS at Largo"},
        new Object[] {"series_race/grand_prix/illegal_category_change_age_increase_too_high", (Function<AbstractRaceTest, String>) _ -> "invalid category change: runner 'Fictional Runner3' changed from MS to M50 during series"},
        new Object[] {"series_race/grand_prix/illegal_category_change_gender_mismatch", (Function<AbstractRaceTest, String>) _ -> "invalid category change: runner 'Fictional Runner4' changed from MS to FS at Sandy Slither"},
        new Object[] {"series_race/grand_prix/missing_property_race_categories_path", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_RACE_CATEGORIES_PATH}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"series_race/grand_prix/missing_property_race_temporal_order", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_RACE_TEMPORAL_ORDER}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"series_race/grand_prix/missing_property_qualifying_clubs", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_QUALIFYING_CLUBS}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"series_race/grand_prix/missing_property_score_for_median_position", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_SCORE_FOR_MEDIAN_POSITION}' in file '\{race_test.config_file_path.getFileName()}'"}
    };

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        GrandPrixRace.main(args);
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
