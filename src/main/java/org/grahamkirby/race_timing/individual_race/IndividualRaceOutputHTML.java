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
import org.grahamkirby.race_timing.series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

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

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "combined", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesWithHeader(writer, race, PrizeResultPrinter::new);
            printTeamPrizes(writer, race);
            printResultsWithHeader(writer, race, OverallResultPrinter::new);
        }
    }

    public static void printPrizesWithHeader(final OutputStreamWriter writer, final Race race, final ResultPrinterGenerator make_prize_result_printer) throws IOException {

        writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);
        writer.append(getPrizesHeader(race));

        printPrizes(writer, race, make_prize_result_printer);
    }

    public static void printResultsWithHeader(final OutputStreamWriter writer, final Race race, final ResultPrinterGenerator make_overall_result_printer) throws IOException {

        writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);

        IndividualRaceResultsOutput.printResults(writer, make_overall_result_printer.apply(race, writer), IndividualRaceOutputHTML::getResultsSubHeader, race);
        writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    public static String getResultsSubHeader(final String s) {
        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    public static String getPrizesHeader(final Race race) {

        final String header = race.getSpecific() instanceof final SeriesRace series_race && series_race.getNumberOfRacesTakenPlace() < (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) ? "Current Standings" : "Prizes";
        return "<h4>" + header + "</h4>" + LINE_SEPARATOR;
    }

    void printPrizes() throws IOException {

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "prizes", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeader(race));
            printPrizes(writer, race, PrizeResultPrinter::new);
            printTeamPrizes(writer, race);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    public static void printPrizes(final OutputStreamWriter writer, final Race race, final ResultPrinterGenerator make_prize_result_printer) throws IOException {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                       // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(writer, category, race, make_prize_result_printer));
    }

    /** Prints prizes within a given category. */
    public static void printPrizes(final OutputStreamWriter writer, final PrizeCategory category, final Race race, final ResultPrinterGenerator make_prize_result_printer) {

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

    private static final class OverallResultPrinter extends IndividualResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Club", "Category", "Time");
        }
    }

    private static final class PrizeResultPrinter extends PrizeResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).club;
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return renderDuration((RaceResultWithDuration) result, DNF_STRING);
        }
    }
}
