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

import org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace;
import org.junit.jupiter.api.Test;

import static org.grahamkirby.race_timing.individual_race.TimedRaceInput.KEY_RAW_RESULTS_PATH;
import static org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace.*;

class GrandPrixTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        GrandPrixRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void legalCategoryChange() throws Exception {
        testExpectedCompletion("series_race/grand_prix/legal_category_change");
    }

    @Test
    void unknownCategory() throws Exception {
        testExpectedCompletion("series_race/grand_prix/unknown_category");
    }

    @Test
    void illegalCategoryChangeAgeReduction() throws Exception {
        testExpectedErrorMessage("series_race/grand_prix/illegal_category_change_age_reduction", () -> "invalid category change: runner 'Fictional Runner2' changed from F40 to FS at Largo");
    }

    @Test
    void illegalCategoryChangeAgeIncreaseTooHigh() throws Exception {
        testExpectedErrorMessage("series_race/grand_prix/illegal_category_change_age_increase_too_high", () -> "invalid category change: runner 'Fictional Runner3' changed from MS to M50 during series");
    }

    @Test
    void illegalCategoryChangeGenderMismatch() throws Exception {
        testExpectedErrorMessage("series_race/grand_prix/illegal_category_change_gender_mismatch", () -> "invalid category change: runner 'Fictional Runner4' changed from MS to FS at Sandy Slither");
    }

    @Test
    void missingPropertyRaceCategoriesPath() throws Exception {
        testExpectedErrorMessage("series_race/grand_prix/missing_property_race_categories_path", () -> STR."no entry for key '\{KEY_RACE_CATEGORIES_PATH}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyRaceTemporalOrder() throws Exception {
        testExpectedErrorMessage("series_race/grand_prix/missing_property_race_temporal_order", () -> STR."no entry for key '\{KEY_RACE_TEMPORAL_ORDER}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyQualifyingClubs() throws Exception {
        testExpectedErrorMessage("series_race/grand_prix/missing_property_qualifying_clubs", () -> STR."no entry for key '\{KEY_QUALIFYING_CLUBS}' in file '\{config_file_path.getFileName()}'");
    }

    @Test
    void missingPropertyScoreForMedianPosition() throws Exception {
        testExpectedErrorMessage("series_race/grand_prix/missing_property_score_for_median_position", () -> STR."no entry for key '\{KEY_SCORE_FOR_MEDIAN_POSITION}' in file '\{config_file_path.getFileName()}'");
    }
}
