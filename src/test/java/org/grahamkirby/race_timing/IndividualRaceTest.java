/*
 * Copyright 2024 Graham Kirby:
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
    public void deadHeats() throws Exception {
        testExpectedCompletion("individual_race/dead_heats");
    }

    @Test
    public void DNFs() throws Exception {
        testExpectedCompletion("individual_race/dnfs");
    }

    @Test
    public void duplicateBibNumber() throws Exception {
        testExpectedException("individual_race/duplicate_bib_number", "duplicate bib number: 3");
    }

    @Test
    public void duplicateRunner() throws Exception {
        testExpectedException("individual_race/duplicate_runner", "duplicate entry: John Smith, Fife AC");
    }

    @Test
    public void duplicateRunnerName() throws Exception {
        testExpectedCompletion("individual_race/duplicate_runner_name");
    }

    @Test
    public void illegalCategory() throws Exception {
        testExpectedException("individual_race/illegal_category", "illegal category for runner: 92");
    }

    @Test
    public void illegalRawTime() throws Exception {
        testExpectedException("individual_race/illegal_raw_time", "illegal time: XXX");
    }

    @Test
    public void multipleTimeFormats() throws Exception {
        testExpectedCompletion("individual_race/multiple_time_formats");
    }

    @Test
    public void resultsOutOfOrder() throws Exception {
        testExpectedException("individual_race/results_out_of_order", "result 15 out of order");
    }

    @Test
    public void seniorNotOpenCategory() throws Exception {
        testExpectedCompletion("individual_race/senior_not_open_category");
    }

    @Test
    public void unregisteredRunner() throws Exception {
        testExpectedException("individual_race/unregistered_runner", "unregistered bib number: 4");
    }

    @Test
    public void alternativeClubNameNormalisation() throws Exception {
        testExpectedCompletion("individual_race/alternative_club_name_normalisation");
    }

    @Test
    public void alternativeHtmlEntityNormalisation() throws Exception {
        testExpectedCompletion("individual_race/alternative_html_entity_normalisation");
    }

    @Test
    public void alternativePrizeReportingOrder() throws Exception {
        testExpectedCompletion("individual_race/alternative_prize_reporting_order");
    }

    @Test
    public void openWinnerFromOlderCategory() throws Exception {
        testExpectedCompletion("individual_race/open_winner_from_older_category");
    }
}
