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
import org.grahamkirby.race_timing.relay_race.RelayRace;
import org.junit.jupiter.api.Test;

public class ActualRacesRelayTest extends RaceTest {

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        RelayRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void devilsBurdens2020() throws Exception {
        testExpectedCompletion("actual_races/relay_race/devils_burdens/2020");
    }

    @Test
    void devilsBurdens2024() throws Exception {
        testExpectedCompletion("actual_races/relay_race/devils_burdens/2024");
    }
}
