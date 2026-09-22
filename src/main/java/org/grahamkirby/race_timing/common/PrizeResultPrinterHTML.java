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
package org.grahamkirby.race_timing.common;


import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Config.*;

public class PrizeResultPrinterHTML extends ResultPrinter {

    public PrizeResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printResultsHeader() throws IOException {

        writer.append(UL_OPEN).append(LINE_SEPARATOR);
    }

    @Override
    public void printResult(final RaceResult result) throws IOException {

        writer.append("    ").
            append(LI_OPEN).append(result.getPositionString()).append(" ").
            append(race_results.getNormalisationProcessor().htmlEncode(String.valueOf(result))).
            append(LI_CLOSE).append(LINE_SEPARATOR);
    }

    @Override
    public void printResultsFooter() throws IOException {

        writer.append(UL_CLOSE).append(LINE_SEPARATOR).append(LINE_SEPARATOR);
    }

    @Override
    public void printNoResults() throws IOException {

        writer.append(PARA_OPEN + HEADING_NO_RESULTS + PARA_CLOSE).append(LINE_SEPARATOR);
    }
}
