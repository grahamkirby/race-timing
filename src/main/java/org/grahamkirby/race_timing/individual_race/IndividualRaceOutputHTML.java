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


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinter;
import org.grahamkirby.race_timing.common.SingleRaceResult;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.SOFTWARE_CREDIT_LINK_TEXT;
import static org.grahamkirby.race_timing.common.Config.*;

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

        try (final OutputStreamWriter writer = new OutputStreamWriter(IndividualRaceResultsOutput.getOutputStream(race, "combined"))) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(SeriesRaceOutputHTML.getPrizesHeader());
            printPrizes(writer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            final ResultPrinter printer = new OverallResultPrinter(race, writer);

            IndividualRaceResultsOutput.printResults(writer, printer, SeriesRaceOutputHTML::getResultsSubHeader, race);

            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    void printPrizes() throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(IndividualRaceResultsOutput.getOutputStream(race, "prizes"))) {

            writer.append(SeriesRaceOutputHTML.getPrizesHeader());
            printPrizes(writer);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    private void printPrizes(final OutputStreamWriter writer) throws IOException {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                       // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> SeriesRaceOutputHTML.printPrizes(writer, category, race, PrizeResultPrinter::new));

        printTeamPrizes(writer);
    }

    private void printTeamPrizes(final OutputStreamWriter writer) throws IOException {

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

    private static final class PrizeResultPrinter extends IndividualResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;
            writer.append("    <li>" + result.getPositionString() + " " + race.getNormalisation().htmlEncode(result.getParticipantName()) + " (" + ((Runner) result.getParticipant()).club + ") " + renderDuration(result, DNF_STRING) + "</li>" + LINE_SEPARATOR);
//            writer.append("    <li>" + result.getPositionString() + " " + race.getNormalisation().htmlEncode(result.entry.participant.name) + " (" + ((Runner)result.entry.participant).club + ") " + renderDuration(result, DNF_STRING) + "</li>" + LINE_SEPARATOR);
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
    }
}
