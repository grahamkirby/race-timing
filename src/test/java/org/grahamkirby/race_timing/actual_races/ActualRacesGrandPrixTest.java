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
package org.grahamkirby.race_timing.actual_races;

import org.grahamkirby.race_timing.RaceTest;
import org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ActualRacesGrandPrixTest extends RaceTest {

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        GrandPrixRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // TODO check ground truth
    @Test
    @Disabled
    void grandPrix2016Completed6() throws Exception {
        testExpectedCompletion("actual_races/series_race/grand_prix/2016/completed_6");
    }

    @Test
    @Disabled
    void grandPrix2016Completed10() throws Exception {
        testExpectedCompletion("actual_races/series_race/grand_prix/2016/completed_10");
    }

    @Test
    void grandPrix2016Completed12() throws Exception {
        testExpectedCompletion("actual_races/series_race/grand_prix/2016/completed_12");
    }
}
