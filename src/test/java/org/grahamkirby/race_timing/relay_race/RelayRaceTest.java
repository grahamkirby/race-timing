/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.relay_race;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.grahamkirby.race_timing.RaceTest.getPathRelativeToProjectRoot;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RelayRaceTest {

    @Test
    public void relayRaceUnsupportedOperations() throws IOException {

        final Path config_file_path = getPathRelativeToProjectRoot("/src/test/resources/synthetic/relay_race/html_output/input/config.txt");

        final RelayRace race = (RelayRace) new RelayRaceFactory().makeRace(config_file_path);

        assertThrows(
            UnsupportedOperationException.class,
            race::outputPreRaceFiles
        );

        assertThrows(
            UnsupportedOperationException.class,
            race::getOverallResults
        );
    }
}
