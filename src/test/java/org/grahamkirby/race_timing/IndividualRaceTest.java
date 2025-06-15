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

import org.grahamkirby.race_timing.individual_race.TimedIndividualRace;
import org.grahamkirby.race_timing.individual_race.TimedRace;
import org.junit.jupiter.api.Test;

import static org.grahamkirby.race_timing.common.Race.*;
import static org.grahamkirby.race_timing.individual_race.TimedRaceInput.KEY_ENTRIES_PATH;
import static org.grahamkirby.race_timing.individual_race.TimedRaceInput.KEY_RAW_RESULTS_PATH;
import static org.grahamkirby.race_timing.relay_race.RelayRace.KEY_PAIRED_LEGS;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

public class IndividualRaceTest extends AbstractRaceTest {

    public static final String[] NON_EXISTENT_CONFIG = {"non-existent-config-file"};

    @Override
    protected void invokeMain(final String[] args) throws Exception {
//        TimedRace.main(args);
        TimedIndividualRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void deadHeats() throws Exception {
        testExpectedCompletion("individual_race/dead_heats");
    }

    @Test
    void DNFs() throws Exception {
        testExpectedCompletion("individual_race/dnfs");
    }

    @Test
    void duplicateRunnerName() throws Exception {
        testExpectedCompletion("individual_race/duplicate_runner_name");
    }

    @Test
    void multipleGender() throws Exception {
        testExpectedCompletion("individual_race/multiple_gender");
    }

    @Test
    void multipleTimeFormats() throws Exception {
        testExpectedCompletion("individual_race/multiple_time_formats");
    }

    @Test
    void nonExclusivePrizeCategories() throws Exception {
        testExpectedCompletion("individual_race/non_exclusive_prize_categories");
    }

    @Test
    void seniorNotOpenCategory() throws Exception {
        testExpectedCompletion("individual_race/senior_not_open_category");
    }

    @Test
    void alternativeClubNameNormalisation() throws Exception {
        testExpectedCompletion("individual_race/alternative_club_name_normalisation");
    }

    @Test
    void alternativeHtmlEntityNormalisation() throws Exception {
        testExpectedCompletion("individual_race/alternative_html_entity_normalisation");
    }

    @Test
    void alternativeCapitalisationStopWords() throws Exception {
        testExpectedCompletion("individual_race/alternative_capitalisation_stop_words");
    }

    @Test
    void alternativePrizeReportingOrder() throws Exception {
        testExpectedCompletion("individual_race/alternative_prize_reporting_order");
    }

    @Test
    void openWinnerFromOlderCategory() throws Exception {
        testExpectedCompletion("individual_race/open_winner_from_older_category");
    }

    @Test
    void prizeCategoryGroups() throws Exception {
        testExpectedCompletion("individual_race/prize_category_groups");
    }

    @Test
    void nameIncludesComma() throws Exception {
        testExpectedCompletion("individual_race/name_includes_comma");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void duplicateBibNumberEntered() throws Exception {
        testExpectedErrorMessage("individual_race/duplicate_bib_number_entered", () -> STR."duplicate bib number '3' in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void duplicateBibNumberRecorded() throws Exception {
        testExpectedErrorMessage("individual_race/duplicate_bib_number_recorded", () -> STR."duplicate bib number '3' at line 6 in file '\{getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'");
    }

    @Test
    void duplicateRunner() throws Exception {
        testExpectedErrorMessage("individual_race/duplicate_runner", () -> STR."duplicate entry 'John Smith, Fife AC' in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void invalidCategory() throws Exception {
        testExpectedErrorMessage("individual_race/invalid_category", () -> STR."invalid entry '92 Hannah Tippetts Dundee Road Runners XXX' at line 92 in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void invalidDNF() throws Exception {
        testExpectedErrorMessage("individual_race/invalid_dnf", () -> STR."invalid entry 'XXX' for key '\{KEY_DNF_FINISHERS}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void invalidEntry() throws Exception {
        testExpectedErrorMessage("individual_race/invalid_entry", () -> STR."invalid entry '138\tRobbie Dunlop\tDundee Road Runners MS' at line 28 in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void invalidRawTime() throws Exception {
        testExpectedErrorMessage("individual_race/invalid_raw_time", () -> STR."invalid record '3\tXXX' at line 4 in file '\{getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'");
    }

    @Test
    void missingConfigFile() throws Exception {
        testExpectedErrorMessage(NON_EXISTENT_CONFIG, () -> "missing config file: 'non-existent-config-file'");
    }

    @Test
    void missingPropertyYear() throws Exception {
        testExpectedErrorMessage("race/missing_property_year", () -> STR."no entry for key '\{KEY_YEAR}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyRaceNameForResults() throws Exception {
        testExpectedErrorMessage("race/missing_property_race_name_for_results", () -> STR."no entry for key '\{KEY_RACE_NAME_FOR_RESULTS}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyRaceNameForFilenames() throws Exception {
        testExpectedErrorMessage("race/missing_property_race_name_for_filenames", () -> STR."no entry for key '\{KEY_RACE_NAME_FOR_FILENAMES}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyCategoriesEntryPath() throws Exception {
        testExpectedErrorMessage("race/missing_property_categories_entry_path", () -> STR."no entry for key '\{KEY_CATEGORIES_ENTRY_PATH}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyCategoriesPrizePath() throws Exception {
        testExpectedErrorMessage("race/missing_property_categories_prize_path", () -> STR."no entry for key '\{KEY_CATEGORIES_PRIZE_PATH}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void resultsOutOfOrder() throws Exception {
        testExpectedErrorMessage("individual_race/results_out_of_order", () -> STR."result out of order at line 5 in file '\{getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'");
    }

    @Test
    void unregisteredRunner() throws Exception {
        testExpectedErrorMessage("individual_race/unregistered_runner", () -> STR."invalid bib number '4' in file '\{getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'");
    }

    @Test
    void missingPropertyEntriesPath() throws Exception {
        testExpectedErrorMessage("individual_race/missing_property_entries_path", () -> STR."no entry for key '\{KEY_ENTRIES_PATH}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyRawResultsPath() throws Exception {
        testExpectedErrorMessage("individual_race/missing_property_raw_results_path", () -> STR."no entry for key '\{KEY_RAW_RESULTS_PATH}' in file '\{config_file_path.getFileName()}'");
    }
}
