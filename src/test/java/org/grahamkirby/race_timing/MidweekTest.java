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

import org.grahamkirby.race_timing.series_race.midweek.MidweekRace;
import org.junit.jupiter.api.Test;

import static org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace.KEY_QUALIFYING_CLUBS;
import static org.grahamkirby.race_timing.series_race.midweek.MidweekRace.KEY_SCORE_FOR_FIRST_PLACE;

public class MidweekTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(String[] args) throws Exception {
        MidweekRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void deadHeats() throws Exception {
        testExpectedCompletion("series_race/midweek/dead_heats");
    }

    @Test
    void duplicateRunnerName() throws Exception {
        testExpectedCompletion("series_race/midweek/duplicate_runner_name");
    }

    @Test
    void prizeCategoryGroups() throws Exception {
        testExpectedCompletion("series_race/midweek/prize_category_groups");
    }

    @Test
    void largeRace() throws Exception {
        testExpectedCompletion("series_race/midweek/large_race");
    }

    @Test
    void nameIncludesComma() throws Exception {
        testExpectedCompletion("series_race/midweek/name_includes_comma");
    }

    @Test
    void missingPropertyKeyScoreForFirstPlace() throws Exception {
        testExpectedErrorMessage("series_race/midweek/missing_property_score_for_first_place", () -> STR."no entry for key '\{KEY_SCORE_FOR_FIRST_PLACE}' in file '\{config_file_path.getFileName()}'");
    }
}
