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

class IndividualPositionsOutput extends RaceOutput {

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

    public IndividualPositionsOutput(final RaceInternal race) {
        super(race);
    }

    private static final class OverallResultPrinterCSV extends ResultPrinter {

        private OverallResultPrinterCSV(final RaceInternal race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("Pos,Runner,Club,Category,").
                append(((SeriesRace) race).getConcatenatedRaceNames()).
                append(",Total,Completed").
                append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SeriesRaceResult result = ((SeriesRaceResult) r);
            final Runner runner = (Runner) result.getParticipant();
            final SeriesRaceScorer scorer = ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getScorer();

            writer.append(result.getPositionString()).append(",").
                append(encode(result.getParticipantName())).append(",").
                append(encode((runner).getClub())).append(",").
                append(result.getParticipant().getCategory().getShortName()).append(",");

            // Iterate over the races rather than the scores within the result, so that future races can be filtered out.
            // A zero score could be due to a runner completing a long way down a large race, rather than the race not having happened.
            writer.append(
                ((SeriesRace) race).getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> scorer.getIndividualRacePerformance(runner, individual_race)).
                    map(IndividualTimesOutput::renderScore).
                    collect(Collectors.joining(","))
            );

            writer.append(",").
                append(String.valueOf(scorer.getSeriesPerformance(runner))).
                append(",").
                append(result.hasCompletedSeries() ? "Y" : "N").append(LINE_SEPARATOR);
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

            final SeriesRaceResult result = (SeriesRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();
            final SeriesRaceScorer scorer = ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getScorer();

            elements.add(result.getPositionString());
            elements.add(race.getNormalisation().htmlEncode(runner.getName()));
            elements.add(runner.getCategory().getShortName());

            elements.add(runner.getClub());

            for (final SingleRaceInternal individual_race : ((SeriesRace) race).getRaces())
                if (individual_race != null) {
                    final Performance score = scorer.getIndividualRacePerformance(runner, individual_race);
                    // TODO move.
                    elements.add(IndividualTimesOutput.renderScore(score));
                }

            elements.add(String.valueOf(scorer.getSeriesPerformance(runner)));
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
        protected String renderPerformance(final RaceResult r) {

            final SeriesRaceResult result = (SeriesRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();
            final SeriesRaceScorer scorer = ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getScorer();

            return String.valueOf(scorer.getSeriesPerformance(runner));
        }
    }
}
