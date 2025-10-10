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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.ResultsOutput;

import java.io.IOException;

@SuppressWarnings("preview")
public class RelayRaceResultsOutput implements ResultsOutput {

    private RelayRaceOutput output;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void outputResults() throws IOException {

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printCollatedTimes();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public void setRace(final Race race) {

        output = new RelayRaceOutput(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        output.printResultsCSV();
        output.printResultsHTML();
    }

    private void printDetailedResults() throws IOException {

        output.printDetailedResultsCSV();
        output.printDetailedResultsHTML();
    }

    private void printLegResults() throws IOException {

        output.printLegResultsCSV();
        output.printLegResultsHTML();
    }

    private void printCollatedTimes() throws IOException {

        output.printCollatedResultsText();
    }

    private void printPrizes() throws IOException {

        output.printPrizesPDF();
        output.printPrizesHTML();
        output.printPrizesText();
    }

    private void printNotes() throws IOException {

        output.printNotes();
    }

    private void printCombined() throws IOException {

        output.printCombinedHTML();
    }
}
