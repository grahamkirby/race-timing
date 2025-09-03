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
package org.grahamkirby.race_timing.actual_races;


import org.grahamkirby.race_timing.AbstractRaceTest;
import org.grahamkirby.race_timing.individual_race.TimedIndividualRace;
import org.grahamkirby.race_timing.series_race.tour.TourRace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Race.loadProperties;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_RACES;

public class ActualRacesMinitourTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(String[] args) throws Exception {

        // Path to configuration file should be first argument.

        Properties properties = loadProperties(Path.of(args[0]));


        if (properties.containsKey(KEY_RACES))
            TourRace.main(args);
        else
            TimedIndividualRace.main(args);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static List<String> getTestCases() throws IOException {

        return getTestCasesWithin("actual_races/series_race/minitour");
    }

//    @ParameterizedTest
//    @MethodSource("getTestCases")
    void testFromDirectories(final String test_directory_path_string) throws Exception {

        testExpectedCompletionNew(test_directory_path_string);
    }
}
