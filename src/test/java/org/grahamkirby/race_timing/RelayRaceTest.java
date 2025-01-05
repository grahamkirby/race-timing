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
    void simple() throws IOException {
        testExpectedCompletion("relay_race/simple");
    }

    @Test
    void legs3() throws IOException {
        testExpectedCompletion("relay_race/legs_3");
    }

    @Test
    void legs5() throws IOException {
        testExpectedCompletion("relay_race/legs_5");
    }

    @Test
    void topTwoResultsWomen() throws IOException {
        testExpectedCompletion("relay_race/top_two_results_women");
    }

    @Test
    void deadHeats() throws IOException {
        testExpectedCompletion("relay_race/dead_heats");
    }

    @Test
    void htmlOutput() throws IOException {
        testExpectedCompletion("relay_race/html_output");
    }

    @Test
    void startOffset() throws IOException {
        testExpectedCompletion("relay_race/start_offset");
    }

    @Test
    void interpolatedRawTimes() throws IOException {
        testExpectedCompletion("relay_race/interpolated_raw_times");
    }

    @Test
    void guessedMissingBibNumbersA() throws IOException {
        testExpectedCompletion("relay_race/guessed_missing_bib_numbers_a");
    }

    @Test
    void guessedMissingBibNumbersB() throws IOException {
        testExpectedCompletion("relay_race/guessed_missing_bib_numbers_b");
    }

    @Test
    void lastFewResultsNotRecorded() throws IOException {
        testExpectedCompletion("relay_race/last_few_results_not_recorded");
    }

    @Test
    void massStartNoneDNFLeg1() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1");
    }

    @Test
    void massStartNoneDNFLeg1And2And3() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1_2_3");
    }

    @Test
    void massStartNoneDNFButCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1_2_3_4a");
    }

    @Test
    void massStartNoneDNFNotCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_1_2_3_4b");
    }

    @Test
    void massStartNoneDNFLeg2() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_2");
    }

    @Test
    void massStartNoneDNFLeg3() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_3");
    }

    @Test
    void massStartNoneDNFLeg4() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_4");
    }

    @Test
    void massStartNoneDNFLeg3And4NotCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_3_4a");
    }

    @Test
    void massStartNoneDNFLeg3And4ButCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_none/dnf_leg_3_4b");
    }

    @Test
    void massStart34AllCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/all_completed");
    }

    @Test
    void massStart34DNFLeg1() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1");
    }

    @Test
    void massStart34DNFLeg1And2And3() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1_2_3");
    }

    @Test
    void massStart34DNFButCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1_2_3_4a");
    }

    @Test
    void massStart34DNFNotCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_1_2_3_4b");
    }

    @Test
    void massStart34DNFLeg2() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_2");
    }

    @Test
    void massStart34DNFLeg3() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_3");
    }

    @Test
    void massStart34DNFLeg3And4NoFinishes() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_3_4a");
    }

    @Test
    void massStart34DNFLeg3And4ButCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_3_4b");
    }

    @Test
    void massStart34DNFLeg4() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/dnf_leg_4");
    }

    @Test
    void massStart34FirstLegFinishAfterMassStart3() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/first_leg_finish_after_mass_start_3");
    }

    @Test
    void massStart34FirstLegFinishAfterMassStart4() throws IOException {
        testExpectedCompletion("relay_race/mass_start_3_4/first_leg_finish_after_mass_start_4");
    }

    @Test
    void massStart4AllCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/all_completed");
    }

    @Test
    void massStart4LegsSwapped() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/legs_swapped");
    }

    @Test
    void massStart4DNFLeg1() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1");
    }

    @Test
    void massStart4DNFLeg1And2And3() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1_2_3");
    }

    @Test
    void massStart4DNFButCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1_2_3_4a");
    }

    @Test
    void massStart4DNFNotCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_1_2_3_4b");
    }

    @Test
    void massStart4DNFLeg2() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_2");
    }

    @Test
    void massStart4DNFLeg3() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_3");
    }

    @Test
    void massStart4DNFLeg3And4NoFinishes() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_3_4a");
    }

    @Test
    void massStart4DNFLeg3And4ButCompleted() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_3_4b");
    }

    @Test
    void massStart4DNFLeg4() throws IOException {
        testExpectedCompletion("relay_race/mass_start_4/dnf_leg_4");
    }

    @Test
    void individualRunnerStartTimeLeg1() throws IOException {
        testExpectedCompletion("relay_race/individual_runner_start_time/leg_1");
    }

    @Test
    void individualRunnerStartTimeLeg3() throws IOException {
        testExpectedCompletion("relay_race/individual_runner_start_time/leg_3");
    }

    @Test
    void unregisteredTeam() throws IOException {
        testExpectedException("relay_race/unregistered_team", "unregistered team: 4");
    }

    @Test
    void duplicateTeamNumber() throws IOException {
        testExpectedException("relay_race/duplicate_team_number", "duplicate bib number: 3");
    }

    @Test
    void duplicateTeamName() throws IOException {
        testExpectedException("relay_race/duplicate_team_name", "duplicate entry: Team 2");
    }

    @Test
    void extraResult() throws IOException {
        testExpectedException("relay_race/extra_result", "surplus result recorded for team: 2");
    }

    @Test
    void illegalDNFTime() throws IOException {
        testExpectedException("relay_race/illegal_dnf_time", "illegal DNF time");
    }

    @Test
    void illegalMassStartTime() throws IOException {
        testExpectedException("relay_race/illegal_mass_start_time", "illegal time: XXX");
    }

    @Test
    void illegalRawTime() throws IOException {
        testExpectedException("relay_race/illegal_raw_time", "illegal time: XXX");
    }

    @Test
    void illegalMassStartTimeOrder() throws IOException {
        testExpectedException("relay_race/illegal_mass_start_time_order", "illegal mass start time order");
    }

    @Test
    void illegalCategory() throws IOException {
        testExpectedException("relay_race/illegal_category", "illegal category for team: 3");
    }

    @Test
    void illegalTeamComposition() throws IOException {
        testExpectedException("relay_race/illegal_team_composition", "illegal composition for team: 3");
    }

    @Test
    void switchedResult() throws IOException {
        testExpectedException("relay_race/switched_result", "surplus result recorded for team: 2");
    }

    @Test
    void resultsOutOfOrder() throws IOException {
        testExpectedException("relay_race/results_out_of_order", "result 15 out of order");
    }

    @Test
    void alternativePrizeReportingOrder() throws IOException {
        testExpectedCompletion("relay_race/alternative_prize_reporting_order");
    }

    @Test
    void prizeCategoryGroups() throws IOException {
        testExpectedCompletion("relay_race/prize_category_groups");
    }

    @Test
    void teamNameIncludesComma() throws IOException {
        testExpectedCompletion("relay_race/team_name_includes_comma");
    }
}
