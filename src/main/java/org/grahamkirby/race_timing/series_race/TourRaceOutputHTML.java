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


import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

class TourRaceOutputHTML {

    private final Race race;

    TourRaceOutputHTML(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        RaceOutput.printResults(race, OverallResultPrinter::new);
    }

    void printCombined() throws IOException {

        SeriesRaceOutputHTML.printCombined(race, OverallResultPrinter::new, PrizeResultPrinter::new);
    }

    public void printPrizes() throws IOException {

        SeriesRaceOutputHTML.printPrizes(race, PrizeResultPrinter::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends OverallResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");
            final List<String> headers = new ArrayList<>(common_headers);

            headers.add("Club");

            final List<Race> races = ((SeriesRace) race.getSpecific()).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
                    headers.add("Race " + (i + 1));

            headers.add("Total");

            return headers;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final TourRaceResult result = (TourRaceResult) r;

            final Runner runner = (Runner) result.getParticipant();

            elements.add(result.getPositionString());
            elements.add(race.getNormalisation().htmlEncode(runner.name));
            elements.add(runner.category.getShortName());
            elements.add(runner.club);

            for (final Duration duration : result.times)
                elements.add(renderDuration(duration, "-"));

            elements.add(renderDuration(result, "-"));

            return elements;
        }
    }

    public static final class PrizeResultPrinter extends PrizeResultPrinterHTML {

        public PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).club;
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return renderDuration((RaceResultWithDuration) result, "-");
        }
    }
}
