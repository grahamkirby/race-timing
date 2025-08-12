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


import org.grahamkirby.race_timing.individual_race.TimedIndividualRace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.*;
import static org.grahamkirby.race_timing.individual_race.TimedRaceInput.KEY_ENTRIES_PATH;
import static org.grahamkirby.race_timing.individual_race.TimedRaceInput.KEY_RAW_RESULTS_PATH;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

public class IndividualRaceTest extends AbstractRaceTest {

    public static final String NON_EXISTENT_CONFIG = "non-existent-config-file";

    private static final List<String> TESTS_EXPECTED_TO_COMPLETE = List.of(
        "individual_race/alternative_capitalisation_stop_words",
        "individual_race/alternative_club_name_normalisation",
        "individual_race/alternative_html_entity_normalisation",
        "individual_race/alternative_prize_reporting_order",
        "individual_race/dead_heats",
        "individual_race/dnfs",
        "individual_race/duplicate_runner_name",
        "individual_race/multiple_gender",
        "individual_race/multiple_time_formats",
        "individual_race/name_includes_comma",
        "individual_race/non_exclusive_prize_categories",
        "individual_race/open_winner_from_older_category",
        "individual_race/prize_category_groups",
        "individual_race/senior_not_open_category"
    );

    private static final Object[][] TESTS_EXPECTED_TO_GIVE_ERROR = new Object[][] {
        new Object[] {"race/missing_property_year", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_YEAR}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"race/missing_property_race_name_for_results", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_RACE_NAME_FOR_RESULTS}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"race/missing_property_race_name_for_filenames", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_RACE_NAME_FOR_FILENAMES}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"race/missing_property_categories_entry_path", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_CATEGORIES_ENTRY_PATH}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"race/missing_property_categories_prize_path", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_CATEGORIES_PRIZE_PATH}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"individual_race/duplicate_bib_number_entered", (Function<AbstractRaceTest, String>) race_test -> STR."duplicate bib number '3' in file '\{race_test.getFileNameForPathProperty(KEY_ENTRIES_PATH)}'"},
        new Object[] {"individual_race/duplicate_bib_number_recorded", (Function<AbstractRaceTest, String>) race_test -> STR."duplicate bib number '3' at line 6 in file '\{race_test.getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'"},
        new Object[] {"individual_race/duplicate_runner", (Function<AbstractRaceTest, String>) race_test -> STR."duplicate entry 'John Smith, Fife AC' in file '\{race_test.getFileNameForPathProperty(KEY_ENTRIES_PATH)}'"},
        new Object[] {"individual_race/invalid_category", (Function<AbstractRaceTest, String>) race_test -> STR."invalid entry '92 Hannah Tippetts Dundee Road Runners XXX' at line 92 in file '\{race_test.getFileNameForPathProperty(KEY_ENTRIES_PATH)}'"},
        new Object[] {"individual_race/invalid_dnf", (Function<AbstractRaceTest, String>) race_test -> STR."invalid entry 'XXX' for key '\{KEY_DNF_FINISHERS}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"individual_race/invalid_entry", (Function<AbstractRaceTest, String>) race_test -> STR."invalid entry '138\tRobbie Dunlop\tDundee Road Runners MS' at line 28 in file '\{race_test.getFileNameForPathProperty(KEY_ENTRIES_PATH)}'"},
        new Object[] {"individual_race/invalid_raw_time", (Function<AbstractRaceTest, String>) race_test -> STR."invalid record '3\tXXX' at line 4 in file '\{race_test.getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'"},
        new Object[] {"individual_race/missing_property_entries_path", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_ENTRIES_PATH}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"individual_race/missing_property_raw_results_path", (Function<AbstractRaceTest, String>) race_test -> STR."no entry for key '\{KEY_RAW_RESULTS_PATH}' in file '\{race_test.config_file_path.getFileName()}'"},
        new Object[] {"individual_race/results_out_of_order", (Function<AbstractRaceTest, String>) race_test -> STR."result out of order at line 5 in file '\{race_test.getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'"},
        new Object[] {"individual_race/unregistered_runner", (Function<AbstractRaceTest, String>) race_test -> STR."invalid bib number '4' in file '\{race_test.getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'"}
    };

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        TimedIndividualRace.main(args);
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

    @Test
    void missingConfigFile() throws Exception {

        // This call bypasses the normal setup phase of copying the source and expected files.
        testExpectedErrorMessage(new String[]{"individual_race/missing_config_file"}, _ -> "missing config file: 'individual_race/missing_config_file'");
    }
}
