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
package org.grahamkirby.race_timing_experimental.series_race;

import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResult;
import org.grahamkirby.race_timing_experimental.common.ResultsOutput;

import java.io.IOException;

@SuppressWarnings("preview")
public class GrandPrixRaceResultsOutput implements ResultsOutput {

    private Race race;

    private GrandPrixRaceOutputCSV output_CSV;
    private GrandPrixRaceOutputHTML output_HTML;
    private GrandPrixRaceOutputText output_text;
    private GrandPrixRaceOutputPDF output_PDF;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void outputResults() throws IOException {

        printOverallResults();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public void setRace(Race race) {

        this.race = race;

        output_CSV = new GrandPrixRaceOutputCSV(race);
        output_HTML = new GrandPrixRaceOutputHTML(race);
        output_text = new GrandPrixRaceOutputText(race);
        output_PDF = new GrandPrixRaceOutputPDF(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();

        for (final RaceResult result : race.getResultsCalculator().getOverallResults())
            if (((GrandPrixRaceResult) result).runner.category == null)
                race.appendToNotes(STR."""
                    Runner \{((GrandPrixRaceResult) result).runner.name} unknown category so omitted from overall results
                    """);
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
