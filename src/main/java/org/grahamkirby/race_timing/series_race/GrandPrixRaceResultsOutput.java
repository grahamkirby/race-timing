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
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultsOutput;
import org.grahamkirby.race_timing.relay_race.RelayRaceOutput;

import java.io.IOException;

import static org.grahamkirby.race_timing.common.Config.LINE_SEPARATOR;

@SuppressWarnings("preview")
public class GrandPrixRaceResultsOutput implements ResultsOutput {

    private Race race;

    private GrandPrixRaceOutputCSV output_CSV;
    private GrandPrixRaceOutputHTML output_HTML;
    private SeriesRaceOutputText output_text;
    private RelayRaceOutput output_PDF;

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

        output_CSV = new GrandPrixRaceOutputCSV(race);
        output_HTML = new GrandPrixRaceOutputHTML(race);
        output_text = new SeriesRaceOutputText(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();

        for (final RaceResult result : race.getResultsCalculator().getOverallResults())
            if (result.getParticipant().category == null)
                race.appendToNotes("Runner " + result.getParticipantName() + " unknown category so omitted from overall results" + LINE_SEPARATOR);
    }

    private void printPrizes() throws IOException {

        RaceOutput.printPrizesPDF(race);
        output_HTML.printPrizes();
        output_text.printPrizesCSV();
    }

    private void printNotes() throws IOException {

        output_text.printNotes();
    }

    private void printCombined() throws IOException {

        output_HTML.printCombined();
    }
}
