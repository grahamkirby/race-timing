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
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.LINE_SEPARATOR;
import static org.grahamkirby.race_timing.common.Config.encode;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

class TourRaceOutput extends RaceOutput {

    @Override
    protected ResultPrinterGenerator getOverallResultCSVPrinterGenerator() {
        return OverallResultPrinterCSV::new;
    }

    @Override
    protected ResultPrinterGenerator getOverallResultHTMLPrinterGenerator() {
        return TourRaceOverallResultPrinterHTML::new;
    }

    @Override
    protected ResultPrinterGenerator getPrizeHTMLPrinterGenerator() {
        return TourRacePrizeResultPrinterHTML::new;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public TourRaceOutput(final RaceInternal race) {
        super(race);
    }

    private static final class OverallResultPrinterCSV extends ResultPrinter {

        private OverallResultPrinterCSV(final RaceInternal race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String race_names = getConcatenatedRaceNames(((SeriesRace) race).getRaces());

            writer.append("Pos,Runner,Club,Category," + race_names + ",Total" + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final TourRaceResult result = (TourRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            writer.append(result.getPositionString()).append(",").
                append(encode(runner.getName())).append(",").
                append(encode(runner.getClub())).append(",").
                append(runner.getCategory().getShortName()).append(",");

            writer.append(
                result.times.stream().
                    map(time -> renderDuration((Duration) time, "-")).
                    collect(Collectors.joining(","))
            );

            writer.append("," + renderDuration(result, "-") + LINE_SEPARATOR);
        }
    }

    private static final class TourRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private TourRaceOverallResultPrinterHTML(final RaceInternal race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");
            final List<String> headers = new ArrayList<>(common_headers);

            headers.add("Club");

            final List<SingleRaceInternal> races = ((SeriesRace) race).getRaces();

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
            elements.add(race.getNormalisation().htmlEncode(runner.getName()));
            elements.add(runner.getCategory().getShortName());
            elements.add(runner.getClub());

            for (final Object duration : result.times)
                elements.add(renderDuration((Duration) duration, "-"));

            elements.add(renderDuration(result, "-"));

            return elements;
        }
    }

    private static final class TourRacePrizeResultPrinterHTML extends PrizeResultPrinterHTML {

        public TourRacePrizeResultPrinterHTML(final RaceInternal race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).getClub();
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return renderDuration((RaceResultWithDuration) result, "-");
        }
    }
}
