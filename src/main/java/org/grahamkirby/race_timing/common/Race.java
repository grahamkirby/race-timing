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
package org.grahamkirby.race_timing.common;

import java.io.IOException;

/**
 * The most general race interface.
 */
public interface Race {

    // TODO fuzz tests.
    // TODO test running from jar.
    // TODO update README (https://www.makeareadme.com).
    // TODO move interpolation from RelayRace to SingleRace.

    /**
     * Processes the race data and generates results.
     * @return a view on the results
     */
    RaceResults processResults();

    /**
     * Outputs the results in various forms to files.
     * @param results the results
     * @throws IOException if the results cannot be written to the appropriate files
     */
    void outputResults(RaceResults results) throws IOException;

    /**
     * Outputs processing notes to file.
     * @throws IOException if the notes cannot be written to file
     */
    void outputNotes() throws IOException;

    /**
     * Outputs a racer list from the entry details, for import into race recording software.
     * @throws IOException if the list cannot be written to file
     */
    void outputRacerList() throws IOException;

    /**
     * Tests whether the configuration file is valid. If not, there is no purpose in further processing.
     * @return true if the configuration file is valid
     */
    boolean configIsValid();
}
