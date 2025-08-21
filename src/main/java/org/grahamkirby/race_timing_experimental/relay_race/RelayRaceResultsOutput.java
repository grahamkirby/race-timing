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
package org.grahamkirby.race_timing_experimental.relay_race;

import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.ResultPrinter;
import org.grahamkirby.race_timing_experimental.common.ResultsOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing_experimental.common.Config.LINE_SEPARATOR;

@SuppressWarnings("preview")
public class RelayRaceResultsOutput implements ResultsOutput {

    private Race race;

    private RelayRaceOutputCSV output_CSV;
    private RelayRaceOutputHTML output_HTML;
    private RelayRaceOutputText output_text;
    private RelayRaceOutputPDF output_PDF;

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
    public void setRace(Race race) {

        this.race = race;

        output_CSV = new RelayRaceOutputCSV(race);
        output_HTML = new RelayRaceOutputHTML(race);
        output_text = new RelayRaceOutputText(race);
        output_PDF = new RelayRaceOutputPDF(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();
    }

    private void printDetailedResults() throws IOException {

        output_CSV.printDetailedResults();
        output_HTML.printDetailedResults();
    }

    private void printLegResults() throws IOException {

        output_CSV.printLegResults();
        output_HTML.printLegResults();
    }

    private void printCollatedTimes() throws IOException {

        output_text.printCollatedResults();
    }

    private void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    private void printNotes() throws IOException {

        output_text.printNotes();
    }

    private void printCombined() throws IOException {

        output_HTML.printCombined();
    }
}
