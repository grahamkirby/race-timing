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
package org.grahamkirby.race_timing_experimental.common;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

/** Base class for printing results to plaintext files. */
public abstract class ResultPrinterText extends ResultPrinter {

    protected ResultPrinterText(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printNoResults() {
        try {
            writer.append("No results").append(LINE_SEPARATOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
