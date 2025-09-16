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
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RaceResultsCalculator;
import org.grahamkirby.race_timing.common.ResultPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Config.STANDARD_FILE_OPEN_OPTIONS;

public class SeriesRaceOutputHTML {

    public static void printResults(final Race race, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_result_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race, "overall"))) {

            final ResultPrinter printer = make_result_printer.apply(race, writer);
            printResults(writer, printer, SeriesRaceOutputHTML::getResultsSubHeader, race);
        }
    }

    public static void printCombined(final Race race, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_result_printer, BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(SeriesRaceOutputHTML.getOutputStream(race, "combined"))) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(SeriesRaceOutputHTML.getPrizesHeader(race));
            SeriesRaceOutputHTML.printPrizes(race, writer, make_prize_printer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            final ResultPrinter printer = make_result_printer.apply(race, writer);
            SeriesRaceOutputHTML.printResults(writer, printer, SeriesRaceOutputHTML::getResultsSubHeader, race);

            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    static void printPrizes(final Race race, BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(race.getOutputDirectoryPath().resolve(STR."\{race_name}_prizes_\{year}.\{HTML_FILE_SUFFIX}"), STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            String prizesHeader = getPrizesHeader(race);
            writer.append(prizesHeader);
            printPrizes(race, writer, make_prize_printer);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    public static void printPrizes(final Race race, final OutputStreamWriter writer, BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) throws IOException {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                       // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(race, writer, category, make_prize_printer));
    }

    /** Prints prizes within a given category. */
    private static void printPrizes(final Race race, final OutputStreamWriter writer, final PrizeCategory category, BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_printer) {

        try {
            writer.append(STR."""
                <p><strong>\{category.getLongName()}</strong></p>
                """);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            make_prize_printer.apply(race, writer).print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPrizesHeader(final Race race) {

        final String header = ((SeriesRaceImpl) race.getSpecific()).getNumberOfRacesTakenPlace() < (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) ? "Current Standings" : "Prizes";
        return STR."<h4>\{header}</h4>\{LINE_SEPARATOR}";
    }

    public static OutputStream getOutputStream(final Race race, final String output_type) throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        return Files.newOutputStream(race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.\{HTML_FILE_SUFFIX}"), STANDARD_FILE_OPEN_OPTIONS);
    }

    public static String getResultsSubHeader(final String s) {
        return STR."""
            <p></p>
            <h4>\{s}</h4>
            """;
    }

    static void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header, final Race race) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(LINE_SEPARATOR);
                writer.append(get_results_sub_header.apply(group.group_title()));
            }

            RaceResultsCalculator raceResults = race.getResultsCalculator();
            List<RaceResult> overallResults = raceResults.getOverallResults(group.categories());
            printer.print(overallResults);

            not_first_category_group = true;
        }
    }
}
