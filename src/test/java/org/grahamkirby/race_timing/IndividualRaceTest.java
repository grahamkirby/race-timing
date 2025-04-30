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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class IndividualRaceTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new IndividualRace(config_file_path);
    }

    @Test
    void deadHeats() throws IOException {
        testExpectedCompletion("individual_race/dead_heats");
    }

    @Test
    void DNFs() throws IOException {
        testExpectedCompletion("individual_race/dnfs");
    }

    @Test
    void duplicateBibNumber() throws IOException {
        testExpectedException("individual_race/duplicate_bib_number", "duplicate bib number 3 in file 'entries.txt'");
    }

    @Test
    void duplicateRunner() throws IOException {
        testExpectedException("individual_race/duplicate_runner", "duplicate entry in file 'entries.txt': John Smith, Fife AC");
    }

    @Test
    void duplicateRunnerName() throws IOException {
        testExpectedCompletion("individual_race/duplicate_runner_name");
    }

    @Test
    void illegalCategory() throws IOException {
        testExpectedException("individual_race/illegal_category", "invalid line 92 in file 'entries.txt': 92 Hannah Tippetts Dundee Road Runners XXX");
    }

    @Test
    void illegalEntry() throws IOException {
        testExpectedException("individual_race/illegal_entry", "invalid line 28 in file 'entries.txt': 138 Robbie Dunlop Dundee Road Runners MS");
    }

    @Test
    void illegalRawTime() throws IOException {
        testExpectedException("individual_race/illegal_raw_time", "invalid line 4 in file 'rawtimes.txt': 3\tXXX");
    }

    // Test for illegal bib number in raw times.

    @Test
    void multipleGender() throws IOException {
        testExpectedCompletion("individual_race/multiple_gender");
    }

    @Test
    void multipleTimeFormats() throws IOException {
        testExpectedCompletion("individual_race/multiple_time_formats");
    }

    @Test
    void nonExclusivePrizeCategories() throws IOException {
        testExpectedCompletion("individual_race/non_exclusive_prize_categories");
    }

    @Test
    void resultsOutOfOrder() throws IOException {
        testExpectedException("individual_race/results_out_of_order", "result 15 out of order");
    }

    @Test
    void seniorNotOpenCategory() throws IOException {
        testExpectedCompletion("individual_race/senior_not_open_category");
    }

    @Test
    void unregisteredRunner() throws IOException {
        testExpectedException("individual_race/unregistered_runner", "unregistered bib number: 4");
    }

    @Test
    void alternativeClubNameNormalisation() throws IOException {
        testExpectedCompletion("individual_race/alternative_club_name_normalisation");
    }

    @Test
    void alternativeHtmlEntityNormalisation() throws IOException {
        testExpectedCompletion("individual_race/alternative_html_entity_normalisation");
    }

    @Test
    void alternativeCapitalisationStopWords() throws IOException {
        testExpectedCompletion("individual_race/alternative_capitalisation_stop_words");
    }

    @Test
    void alternativePrizeReportingOrder() throws IOException {
        testExpectedCompletion("individual_race/alternative_prize_reporting_order");
    }

    @Test
    void openWinnerFromOlderCategory() throws IOException {
        testExpectedCompletion("individual_race/open_winner_from_older_category");
    }

    @Test
    void prizeCategoryGroups() throws IOException {
        testExpectedCompletion("individual_race/prize_category_groups");
    }

    @Test
    void nameIncludesComma() throws IOException {
        testExpectedCompletion("individual_race/name_includes_comma");
    }
}
