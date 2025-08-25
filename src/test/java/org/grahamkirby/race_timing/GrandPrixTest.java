/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing;


import org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.series_race.grand_prix.GrandPrixRace.*;

class GrandPrixTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        GrandPrixRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static List<String> getTestCases() throws IOException {

        return getTestCases("series_race/grand_prix");
    }

    @ParameterizedTest
    @MethodSource("getTestCases")
    void testFromDirectories(final String test_directory_path_string) throws Exception {

        testExpectedCompletionNew(test_directory_path_string);
    }
}
