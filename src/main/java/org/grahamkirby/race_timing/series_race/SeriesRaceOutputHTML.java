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

import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinter;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.*;

public class SeriesRaceOutputHTML {

    public static void printResults(final Race race, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_result_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(IndividualRaceResultsOutput.getOutputStream(race, "overall", HTML_FILE_SUFFIX))) {

            final ResultPrinter printer = make_result_printer.apply(race, writer);
            printResults(writer, printer, SeriesRaceOutputHTML::getResultsSubHeader, race);
        }
    }

    public static void printCombined(final Race race, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_result_printer, BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(IndividualRaceResultsOutput.getOutputStream(race, "combined", HTML_FILE_SUFFIX))) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(SeriesRaceOutputHTML.getPrizesHeader(race));
            printPrizes(race, writer, make_prize_printer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            final ResultPrinter printer = make_result_printer.apply(race, writer);
            SeriesRaceOutputHTML.printResults(writer, printer, SeriesRaceOutputHTML::getResultsSubHeader, race);

            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    static void printPrizes(final Race race, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(IndividualRaceResultsOutput.getOutputStream(race, "prizes", HTML_FILE_SUFFIX))) {

            writer.append(getPrizesHeader(race));
            printPrizes(race, writer, make_prize_printer);
        }
    }

    /** Prints prizes within a given category. */
    public static void printPrizes(final OutputStreamWriter writer, final PrizeCategory category, final Race race, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) {

        try {
            writer.append("<p><strong>" + category.getLongName() + "</strong></p>" + LINE_SEPARATOR);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            make_prize_printer.apply(race, writer).print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    public static void printPrizes(final Race race, final OutputStreamWriter writer, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) throws IOException {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                                   // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).                               // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(race, writer, category, make_prize_printer));
    }

    /** Prints prizes within a given category. */
    private static void printPrizes(final Race race, final OutputStreamWriter writer, final PrizeCategory category, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) {

        try {
            writer.append("<p><strong>" + category.getLongName() + "</strong></p>" + LINE_SEPARATOR);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            make_prize_printer.apply(race, writer).print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPrizesHeader(final Race race) {

        final String header = ((SeriesRace) race.getSpecific()).getNumberOfRacesTakenPlace() < (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) ? "Current Standings" : "Prizes";
        return "<h4>" + header + "</h4>" + LINE_SEPARATOR;
    }

    public static String getResultsSubHeader(final String s) {
        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    public static String getPrizesHeader() {
        return "<h4>Prizes</h4>" + LINE_SEPARATOR;
    }

    static void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header, final Race race) throws IOException {

        IndividualRaceResultsOutput.printResults(writer, printer, get_results_sub_header, race);
    }
}
