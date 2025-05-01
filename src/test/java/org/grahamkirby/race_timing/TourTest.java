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

public class TourTest extends RaceTest {

    @Override
    protected void invokeMain(String[] args) throws Exception {
        TourRace.main(args);
    }

    @Test
    void categoryWithoutWinner() throws Exception {
        testExpectedCompletion("series_race/minitour/category_without_winner");
    }

    @Test
    void nameIncludesComma() throws Exception {
        testExpectedCompletion("series_race/minitour/name_includes_comma");
    }
}
