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

import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.junit.jupiter.api.Test;

import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_ENTRIES_PATH;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_RAW_RESULTS_PATH;

public class IndividualRaceTest extends RaceTest {

    @Override
    protected void invokeMain(String[] args) throws Exception {
        IndividualRace.main(args);
    }

    @Test
    void deadHeats() throws Exception {
        testExpectedCompletion("individual_race/dead_heats");
    }

    @Test
    void DNFs() throws Exception {
        testExpectedCompletion("individual_race/dnfs");
    }

    @Test
    void duplicateBibNumber() throws Exception {
        testExpectedErrorMessage("individual_race/duplicate_bib_number", () -> STR."duplicate bib number '3' in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void duplicateRunner() throws Exception {
        testExpectedErrorMessage("individual_race/duplicate_runner", () -> STR."duplicate entry 'John Smith, Fife AC' in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void duplicateRunnerName() throws Exception {
        testExpectedCompletion("individual_race/duplicate_runner_name");
    }

    @Test
    void illegalCategory() throws Exception {
        testExpectedErrorMessage("individual_race/illegal_category", () -> STR."invalid entry '92 Hannah Tippetts Dundee Road Runners XXX' at line 92 in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void illegalEntry() throws Exception {
        testExpectedErrorMessage("individual_race/illegal_entry", () -> STR."invalid entry '138 Robbie Dunlop Dundee Road Runners MS' at line 28 in file '\{getFileNameForPathProperty(KEY_ENTRIES_PATH)}'");
    }

    @Test
    void illegalRawTime() throws Exception {
        testExpectedErrorMessage("individual_race/illegal_raw_time", () -> STR."invalid record '3\tXXX' at line 4 in file '\{getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'");

    }

    // TODO Test for illegal bib number in raw times.

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
    void resultsOutOfOrder() throws Exception {
        testExpectedErrorMessage("individual_race/results_out_of_order", () -> STR."result out of order at line 15 in file '\{getFileNameForPathProperty(KEY_RAW_RESULTS_PATH)}'");
    }

    @Test
    void seniorNotOpenCategory() throws Exception {
        testExpectedCompletion("individual_race/senior_not_open_category");
    }

    @Test
    void unregisteredRunner() throws Exception {
        testExpectedErrorMessage("individual_race/unregistered_runner", () -> "unregistered bib number: 4");
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
}
