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
import org.junit.jupiter.api.Test;
import org.grahamkirby.race_timing.relay_race.RelayRace;

import java.io.IOException;
import java.nio.file.Path;

public class RelayRaceTest extends RaceTest {

    // Synthetic names for most tests generated by:
    // https://www.fakenamegenerator.com/gen-random-gd-uk.php

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new RelayRace(config_file_path);
    }

    @Test
    public void simple() throws Exception {
        testExpectedCompletion("relay_race/simple");
    }

    @Test
    public void legs3() throws Exception {
        testExpectedCompletion("relay_race/legs_3");
    }

    @Test
    public void legs5() throws Exception {
        testExpectedCompletion("relay_race/legs_5");
    }

    @Test
    public void topTwoResultsWomen() throws Exception {
        testExpectedCompletion("relay_race/top_two_results_women");
    }

    @Test
    public void deadHeats() throws Exception {
        testExpectedCompletion("relay_race/dead_heats");
    }

    @Test
    public void htmlOutput() throws Exception {
        testExpectedCompletion("relay_race/html_output");
    }

    @Test
    public void startOffset() throws Exception {
        testExpectedCompletion("relay_race/start_offset");
    }

    @Test
    public void interpolatedRawTimes() throws Exception {
        testExpectedCompletion("relay_race/interpolated_raw_times");
    }

    @Test
    public void guessedMissingBibNumbersA() throws Exception {
        testExpectedCompletion("relay_race/guessed_missing_bib_numbers_a");
    }

    @Test
    public void guessedMissingBibNumbersB() throws Exception {
        testExpectedCompletion("relay_race/guessed_missing_bib_numbers_b");
    }

    @Test
    public void lastFewResultsNotRecorded() throws Exception {
        testExpectedCompletion("relay_race/last_few_results_not_recorded");
    }

    @Test
    public void massStartNoneDNFLeg1() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1");
    }

    @Test
    public void massStartNoneDNFLeg1And2And3() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1_2_3");
    }

    @Test
    public void massStartNoneDNFButCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1_2_3_4a");
    }

    @Test
    public void massStartNoneDNFNotCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1_2_3_4b");
    }

    @Test
    public void massStartNoneDNFLeg2() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_2");
    }

    @Test
    public void massStartNoneDNFLeg3() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_3");
    }

    @Test
    public void massStartNoneDNFLeg4() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_4");
    }

    @Test
    public void massStartNoneDNFLeg3And4NotCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_3_4a");
    }

    @Test
    public void massStartNoneDNFLeg3And4ButCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_3_4b");
    }

    @Test
    public void massStart34AllCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/all_completed");
    }

    @Test
    public void massStart34DNFLeg1() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1");
    }

    @Test
    public void massStart34DNFLeg1And2And3() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1_2_3");
    }

    @Test
    public void massStart34DNFButCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1_2_3_4a");
    }

    @Test
    public void massStart34DNFNotCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1_2_3_4b");
    }

    @Test
    public void massStart34DNFLeg2() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_2");
    }

    @Test
    public void massStart34DNFLeg3() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_3");
    }

    @Test
    public void massStart34DNFLeg3And4NoFinishes() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_3_4a");
    }

    @Test
    public void massStart34DNFLeg3And4ButCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_3_4b");
    }

    @Test
    public void massStart34DNFLeg4() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_4");
    }

    @Test
    public void massStart34FirstLegFinishAfterMassStart3() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/first_leg_finish_after_mass_start_3");
    }

    @Test
    public void massStart34FirstLegFinishAfterMassStart4() throws Exception {
        testExpectedCompletion("relay_race/mass_start_3_4/first_leg_finish_after_mass_start_4");
    }

    @Test
    public void massStart4AllCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/all_completed");
    }

    @Test
    public void massStart4LegsSwapped() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/legs_swapped");
    }

    @Test
    public void massStart4DNFLeg1() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1");
    }

    @Test
    public void massStart4DNFLeg1And2And3() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1_2_3");
    }

    @Test
    public void massStart4DNFButCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1_2_3_4a");
    }

    @Test
    public void massStart4DNFNotCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1_2_3_4b");
    }

    @Test
    public void massStart4DNFLeg2() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_2");
    }

    @Test
    public void massStart4DNFLeg3() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_3");
    }

    @Test
    public void massStart4DNFLeg3And4NoFinishes() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_3_4a");
    }

    @Test
    public void massStart4DNFLeg3And4ButCompleted() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_3_4b");
    }

    @Test
    public void massStart4DNFLeg4() throws Exception {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_4");
    }

    @Test
    public void individualRunnerStartTimeLeg1() throws Exception {
        testExpectedCompletion("relay_race/individual_runner_start_time/leg_1");
    }

    @Test
    public void individualRunnerStartTimeLeg3() throws Exception {
        testExpectedCompletion("relay_race/individual_runner_start_time/leg_3");
    }

    @Test
    public void unregisteredTeam() throws Exception {
        testExpectedException("relay_race/unregistered_team", "unregistered team: 4");
    }

    @Test
    public void duplicateTeamNumber() throws Exception {
        testExpectedException("relay_race/duplicate_team_number", "duplicate bib number: 3");
    }

    @Test
    public void duplicateTeamName() throws Exception {
        testExpectedException("relay_race/duplicate_team_name", "duplicate entry: Team 2");
    }

    @Test
    public void extraResult() throws Exception {
        testExpectedException("relay_race/extra_result", "surplus result recorded for team: 2");
    }

    @Test
    public void illegalDNFTime() throws Exception {
        testExpectedException("relay_race/illegal_dnf_time", "illegal DNF time");
    }

    @Test
    public void illegalMassStartTime() throws Exception {
        testExpectedException("relay_race/illegal_mass_start_time", "illegal time: XXX");
    }

    @Test
    public void illegalRawTime() throws Exception {
        testExpectedException("relay_race/illegal_raw_time", "illegal time: XXX");
    }

    @Test
    public void illegalMassStartTimeOrder() throws Exception {
        testExpectedException("relay_race/illegal_mass_start_time_order", "illegal mass start time order");
    }

    @Test
    public void illegalCategory() throws Exception {
        testExpectedException("relay_race/illegal_category", "illegal category for team: 3");
    }

    @Test
    public void illegalTeamComposition() throws Exception {
        testExpectedException("relay_race/illegal_team_composition", "illegal composition for team: 3");
    }

    @Test
    public void switchedResult() throws Exception {
        testExpectedException("relay_race/switched_result", "surplus result recorded for team: 2");
    }

    @Test
    public void resultsOutOfOrder() throws Exception {
        testExpectedException("relay_race/results_out_of_order","result 15 out of order");
    }

    @Test
    public void alternativePrizeReportingOrder() throws Exception {
        testExpectedCompletion("relay_race/alternative_prize_reporting_order");
    }

    @Test
    public void prizeCategoryGroups() throws Exception {
        testExpectedCompletion("relay_race/prize_category_groups");
    }
}
