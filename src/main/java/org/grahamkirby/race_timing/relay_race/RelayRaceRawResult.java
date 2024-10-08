/*
 * Copyright 2024 Graham Kirby:
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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.RawResult;

public class RelayRaceRawResult extends RawResult {

    // Leg number is optional, depending on whether it was recorded on paper sheet.
    private final int leg_number;

    public RelayRaceRawResult(final String file_line) {

        super(file_line);

        final String[] elements = file_line.split("\t");
        leg_number = elements.length == 2 ? -1 : Integer.parseInt(elements[2]);
    }

    public int getLegNumber() {
        return leg_number;
    }
}
