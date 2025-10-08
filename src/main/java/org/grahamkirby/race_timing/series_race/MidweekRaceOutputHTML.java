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
import org.grahamkirby.race_timing.common.ResultPrinterHTML;
import org.grahamkirby.race_timing.individual_race.IndividualRaceOutputHTML;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsOutput;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MidweekRaceOutputHTML {

    private final Race race;

    MidweekRaceOutputHTML(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        IndividualRaceResultsOutput.printResults(race, OverallResultPrinter::new);
    }

    void printCombined() throws IOException {

        SeriesRaceOutputHTML.printCombined(race, OverallResultPrinter::new, IndividualRaceOutputHTML.PrizeResultPrinter::new);
    }

    public void printPrizes() throws IOException {

        SeriesRaceOutputHTML.printPrizes(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");
            final List<String> headers = new ArrayList<>(common_headers);

            headers.add("Club");

            final List<Race> races = ((MidweekRaceImpl) race.getSpecific()).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
                    headers.add("Race " + (i + 1));

            headers.add("Total");
            headers.add("Completed?");

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final MidweekRaceResultsCalculator calculator = (MidweekRaceResultsCalculator) race.getResultsCalculator();
            final MidweekRaceResult result = (MidweekRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            elements.add(result.getPositionString());
            elements.add(race.getNormalisation().htmlEncode(runner.name));
            elements.add(runner.category.getShortName());

            elements.add(runner.club);

            for (final Race individual_race : ((MidweekRaceImpl) race.getSpecific()).getRaces())
                if (individual_race != null) {
                    final int score = calculator.calculateRaceScore(individual_race, runner);
                    elements.add(String.valueOf(score));
                }

            elements.add(String.valueOf(result.totalScore()));
            elements.add(result.hasCompletedSeries() ? "Y" : "N");

            return elements;
        }
    }
}
