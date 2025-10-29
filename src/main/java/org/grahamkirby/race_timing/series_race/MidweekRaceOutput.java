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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;

class MidweekRaceOutput extends RaceOutput {

    @Override
    protected ResultPrinterGenerator getOverallResultCSVPrinterGenerator() {
        return OverallResultPrinterCSV::new;
    }

    @Override
    protected ResultPrinterGenerator getOverallResultHTMLPrinterGenerator() {
        return MidweekRaceOverallResultPrinterHTML::new;
    }

    @Override
    protected ResultPrinterGenerator getPrizeHTMLPrinterGenerator() {
        return MidweekRacePrizeResultPrinterHTML::new;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MidweekRaceOutput(final RaceInternal race) {
        super(race);
    }

    private static final class OverallResultPrinterCSV extends ResultPrinter {

        private OverallResultPrinterCSV(final RaceInternal race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String race_names = getConcatenatedRaceNames(((SeriesRace) race).getRaces());

            writer.append("Pos,Runner,Club,Category," + race_names + ",Total,Completed" + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult) r);
            final SeriesRaceResultsCalculator calculator = (SeriesRaceResultsCalculator) race.getResultsCalculator();

            writer.append(result.getPositionString() + "," + encode(result.getParticipantName()) + "," + encode(((Runner) result.getParticipant()).getClub()) + "," + result.getParticipant().getCategory().getShortName() + ",");

            // Iterate over the races rather than the scores within the result, so that future races can be filtered out.
            // A zero score could be due to a runner completing a long way down a large race, rather than the race not having happened.
            writer.append(
                ((SeriesRace) race).getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> calculator.scorer.calculateRaceScore((Runner) result.getParticipant(), individual_race)).
                    map(GrandPrixRaceOutput::renderScore).
                    collect(Collectors.joining(","))
            );

            writer.append("," + result.totalScore() + "," + (result.hasCompletedSeries() ? "Y" : "N") + LINE_SEPARATOR);
        }
    }

    private static final class MidweekRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private MidweekRaceOverallResultPrinterHTML(final RaceInternal race, final OutputStreamWriter writer) {
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
            headers.add("Completed?");

            return headers;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final SeriesRaceResultsCalculator calculator = (SeriesRaceResultsCalculator) race.getResultsCalculator();
            final MidweekRaceResult result = (MidweekRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            elements.add(result.getPositionString());
            elements.add(race.getNormalisation().htmlEncode(runner.getName()));
            elements.add(runner.getCategory().getShortName());

            elements.add(runner.getClub());

            for (final SingleRaceInternal individual_race : ((SeriesRace) race).getRaces())
                if (individual_race != null) {
//                    final int score = (int) calculator.scorer.calculateRaceScore(individual_race, runner);
//                    elements.add(String.valueOf(score));
                    final Object score = calculator.scorer.calculateRaceScore(runner, individual_race);
                    elements.add(GrandPrixRaceOutput.renderScore(score));
                }

            elements.add(String.valueOf(result.totalScore()));
            elements.add(result.hasCompletedSeries() ? "Y" : "N");

            return elements;
        }
    }

    private static final class MidweekRacePrizeResultPrinterHTML extends PrizeResultPrinterHTML {

        public MidweekRacePrizeResultPrinterHTML(final RaceInternal race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).getClub();
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return String.valueOf(((MidweekRaceResult) result).totalScore());
        }
    }
}
