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

import org.grahamkirby.race_timing.series_race.tour.TourRace;
import org.junit.jupiter.api.Test;

import static org.grahamkirby.race_timing.series_race.midweek.MidweekRace.KEY_SCORE_FOR_FIRST_PLACE;
import static org.grahamkirby.race_timing.series_race.tour.TourRaceInput.KEY_TIME_TRIAL_RACE;
import static org.grahamkirby.race_timing.series_race.tour.TourRaceInput.KEY_TIME_TRIAL_STARTS;

public class TourTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        TourRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void categoryWithoutWinner() throws Exception {
        testExpectedCompletion("series_race/minitour/category_without_winner");
    }

    @Test
    void nameIncludesComma() throws Exception {
        testExpectedCompletion("series_race/minitour/name_includes_comma");
    }

    @Test
    void duplicateIndividualRace() throws Exception {
        testExpectedErrorMessage("series_race/minitour/duplicate_individual_race", () -> STR."duplicate races specified in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void invalidNumberOfIndividualRaces() throws Exception {
        testExpectedErrorMessage("series_race/minitour/invalid_number_of_individual_races", () -> STR."invalid number of races specified in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void nonExistentIndividualRace() throws Exception {
        testExpectedErrorMessage("series_race/minitour/non_existent_individual_race", () -> STR."invalid config for race 2 in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyKeyTimeTrialRace() throws Exception {
        testExpectedErrorMessage("series_race/minitour/missing_property_time_trial_race", () -> STR."no entry for key '\{KEY_TIME_TRIAL_RACE}' in file '\{config_file_path.getFileName()}'");
    }
    @Test
    void missingPropertyKeyTimeTrialStarts() throws Exception {
        testExpectedErrorMessage("series_race/minitour/missing_property_time_trial_starts", () -> STR."no entry for key '\{KEY_TIME_TRIAL_STARTS}' in file '\{config_file_path.getFileName()}'");
    }
}
