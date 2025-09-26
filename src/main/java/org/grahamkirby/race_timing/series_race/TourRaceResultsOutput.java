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
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultsOutput;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;

import static org.grahamkirby.race_timing.common.Config.LINE_SEPARATOR;

@SuppressWarnings("preview")
public class TourRaceResultsOutput implements ResultsOutput {

    private Race race;

    private TourRaceOutputCSV output_CSV;
    private TourRaceOutputHTML output_HTML;
    private TourRaceOutputText output_text;
    private TourRaceOutputPDF output_PDF;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void outputResults() throws IOException {

        printOverallResults();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public void setRace(final Race race) {

        this.race = race;

        output_CSV = new TourRaceOutputCSV(race);
        output_HTML = new TourRaceOutputHTML(race);
        output_text = new TourRaceOutputText(race);
        output_PDF = new TourRaceOutputPDF(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();

        for (final RaceResult result : race.getResultsCalculator().getOverallResults())
            if (result.getCategory() == null)
                race.appendToNotes(LINE_SEPARATOR + "Runner " + result.getParticipantName() + " unknown category so omitted from overall results" + LINE_SEPARATOR);
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
