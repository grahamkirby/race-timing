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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceOutput;
import org.grahamkirby.race_timing.common.ResultPrinterGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Config.HTML_FILE_SUFFIX;
import static org.grahamkirby.race_timing.common.RaceOutput.getOutputStream;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceOutput.*;

public class SeriesRaceOutputHTML {

//    public static void printCombinedHTML(final Race race, final ResultPrinterGenerator make_result_printer, final ResultPrinterGenerator make_prize_printer) throws IOException {
//
//        final OutputStream stream = getOutputStream(race, "combined", HTML_FILE_SUFFIX);
//
//        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
//
//            printPrizesWithHeaderHTML(writer, race, make_prize_printer);
//            printResultsWithHeaderHTML(writer, race, make_result_printer);
//        }
//    }
//
//    static void printPrizesHTML(final Race race, final ResultPrinterGenerator make_prize_result_printer) throws IOException {
//
//        final OutputStream stream = getOutputStream(race, "prizes", HTML_FILE_SUFFIX);
//
//        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
//
//            writer.append(getPrizesHeaderHTML(race));
//            RaceOutput.printPrizesHTML(race, writer, make_prize_result_printer);
//        }
//    }
}
