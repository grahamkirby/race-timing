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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

public class IndividualRaceOutputHTML {

    private final Race race;

    IndividualRaceOutputHTML(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        IndividualRaceResultsOutput.printResults(race, OverallResultPrinter::new);
    }

    /** Prints all details to a single web page. */
    void printCombined() throws IOException {

        final BiConsumer<Race, OutputStreamWriter> print_team_prizes = (race, writer) -> {
            try {
                printTeamPrizes(writer, race);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        };

        printCombined(race, print_team_prizes, OverallResultPrinter::new, PrizeResultPrinter::new);
    }

    public static void printCombined(final Race race, final BiConsumer<Race, OutputStreamWriter> print_team_prizes, final BiFunction<Race, OutputStreamWriter, ResultPrinter> make_result_printer, BiFunction<Race, OutputStreamWriter, ResultPrinter> prize_result_printer) throws IOException {

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "combined", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(SeriesRaceOutputHTML.getPrizesHeader(race));
            printPrizes(writer, race, prize_result_printer);
            print_team_prizes.accept(race, writer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            final ResultPrinter printer = make_result_printer.apply(race, writer);

            IndividualRaceResultsOutput.printResults(writer, printer, IndividualRaceOutputHTML::getResultsSubHeader, race);

            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    public static String getResultsSubHeader(final String s) {
        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    void printPrizes() throws IOException {

        printPrizes(race);
        printTeamPrizes(race);
    }

    public static void printPrizes(final Race race) throws IOException {

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "prizes", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(SeriesRaceOutputHTML.getPrizesHeader(race));
            printPrizes(writer, race, PrizeResultPrinter::new);
        }
    }

    public static void printTeamPrizes(final Race race) throws IOException {

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "prizes", HTML_FILE_SUFFIX, APPEND_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printTeamPrizes(writer, race);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    public static void printPrizes(final OutputStreamWriter writer, final Race race, BiFunction<Race, OutputStreamWriter, ResultPrinter> aNew) throws IOException {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                       // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(writer, category, race, aNew));
    }

    /** Prints prizes within a given category. */
    public static void printPrizes(final OutputStreamWriter writer, final PrizeCategory category, final Race race, BiFunction<Race, OutputStreamWriter, ResultPrinter> make_prize_result_printer) {

        try {
            writer.append("<p><strong>" + category.getLongName() + "</strong></p>" + LINE_SEPARATOR);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            make_prize_result_printer.apply(race, writer).print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printTeamPrizes(final OutputStreamWriter writer, final Race race) throws IOException {

        final List<String> team_prizes = ((IndividualRaceImpl) race.getSpecific()).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            writer.append("<h4>Team Prizes</h4>").append(LINE_SEPARATOR);
            writer.append("<ul>").append(LINE_SEPARATOR);

            for (final String team_prize : team_prizes)
                writer.append("<li>").append(team_prize).append("</li>").append(LINE_SEPARATOR);

            writer.append("</ul>").append(LINE_SEPARATOR);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final class OverallResultPrinter extends IndividualResultPrinterHTML {

        OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Club", "Category", "Time");
        }
    }

    public static final class PrizeResultPrinter extends PrizeResultPrinterHTML {

        public PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final SingleRaceResult result = (SingleRaceResult) r;

            return List.of(
                result.getPositionString(),
                race.getNormalisation().htmlEncode(result.getParticipantName()),
                ((Runner) result.getParticipant()).club,
                renderDuration(result, DNF_STRING)
            );
        }
    }
}
