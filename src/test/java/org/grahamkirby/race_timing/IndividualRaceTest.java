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


import org.grahamkirby.race_timing.individual_race.TimedIndividualRace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;

import static org.grahamkirby.race_timing_experimental.common.Config.PATH_SEPARATOR;

public class IndividualRaceTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(final String[] args) throws Exception {
        TimedIndividualRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static List<String> getTestCases() throws IOException {

        return getTestCasesWithin("individual_race");
    }

//    @ParameterizedTest
//    @MethodSource("getTestCases")
    void testFromDirectories(final String test_directory_path_string) throws Exception {

        testExpectedCompletionNew(test_directory_path_string);
    }

    @Test
    void missingConfigFile() throws Exception {

        // This call bypasses the normal setup phase of copying the source and expected files.
        testExpectedErrorMessage(new String[]{"special_cases/missing_config_file"}, _ -> STR."missing config file: 'special_cases\{PATH_SEPARATOR}missing_config_file'");
    }
}
