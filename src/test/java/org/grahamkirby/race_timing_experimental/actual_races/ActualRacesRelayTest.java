/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.actual_races;


import org.grahamkirby.race_timing.AbstractRaceTest;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.relay_race.RelayRaceFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ActualRacesRelayTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(final String[] args) throws Exception {

        try {
            final Race relay_race = RelayRaceFactory.makeRelayRace(Path.of(args[0]));
            relay_race.processResults();

        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static List<String> getTestCases() throws IOException {

        return getTestCases("actual_races/relay_race");
    }

    @ParameterizedTest
    @MethodSource("getTestCases")
    void testFromDirectories(final String test_directory_path_string) throws Exception {

        testExpectedCompletionNew(test_directory_path_string);
    }
}
