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

class SeriesRaceOutput extends RaceOutput {

    @Override
    protected ResultPrinterGenerator getOverallResultCSVPrinterGenerator() {
        return SeriesRaceOverallResultPrinterCSV::new;
    }

    @Override
    protected ResultPrinterGenerator getOverallResultHTMLPrinterGenerator() {
        return SeriesRaceOverallResultPrinterHTML::new;
    }

    @Override
    protected ResultPrinterGenerator getPrizeHTMLPrinterGenerator() {
        return SeriesRacePrizeResultPrinterHTML::new;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRaceOutput(final RaceInternal race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static int countClubs(final List<RaceResult> results) {

        return (int) results.stream().
            map(result -> ((Runner) result.getParticipant()).getClub()).
            distinct().
            count();
    }

    private static final class SeriesRaceOverallResultPrinterCSV extends ResultPrinter {

        final SeriesRaceResultsCalculator calculator;
        final boolean multiple_clubs;
        final boolean multiple_race_categories;
        final boolean multiple_races_taken_place;
        final boolean possible_to_have_completed;

        private SeriesRaceOverallResultPrinterCSV(final RaceInternal race, final OutputStreamWriter writer) {

            super(race, writer);
            calculator = (SeriesRaceResultsCalculator) race.getResultsCalculator();
            multiple_clubs = countClubs(calculator.getOverallResults()) > 1;
            multiple_race_categories = calculator.getRaceCategories().size() > 1;
            multiple_races_taken_place = ((SeriesRace) race).getNumberOfRacesTakenPlace() > 1;
            possible_to_have_completed = ((SeriesRace) race).getNumberOfRacesTakenPlace() >= (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String race_names = ((SeriesRace) race).getConcatenatedRaceNames();

            final String race_categories_header = calculator.getRaceCategories().stream().
                map(SeriesRaceCategory::category_title).
                collect(Collectors.joining(","));

            writer.append("Pos,Runner,");
            if (multiple_clubs)
                writer.append("Club,");
            writer.append("Category,").
                append(race_names);
            if (multiple_races_taken_place)
                writer.append(",Total");
            if (possible_to_have_completed)
                writer.append(",Completed");
            if (multiple_race_categories)
                writer.append(",").append(race_categories_header);
            writer.append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SeriesRaceResult result = (SeriesRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();
            final SeriesRaceScorer scorer = calculator.getScorer();

            writer.append(result.getPositionString()).append(",").
                append(encode(runner.getName())).append(",");
            if (multiple_clubs)
                writer.append(encode((runner).getClub())).append(",");
            writer.append(runner.getCategory().getShortName()).append(",");

            // TODO check whether comment below still holds.
            // Iterate over the races rather than the scores within the result, so that future races can be filtered out.
            // A zero score could be due to a runner completing a long way down a large race, rather than the race not having happened.
            writer.append(
                ((SeriesRace) race).getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> scorer.getIndividualRacePerformance(runner, individual_race)).
                    map(SeriesRaceOutput::renderScore).
                    collect(Collectors.joining(","))
            );

            if (multiple_races_taken_place)
                writer.append("," ).append(renderScore(scorer.getSeriesPerformance(runner)));
            if (possible_to_have_completed)
                writer.append(",").append(result.hasCompletedSeries() ? "Y" : "N");

            if (multiple_race_categories)
                writer.append(",").append(
                    calculator.getRaceCategories().stream().
                        map(category -> result.hasCompletedRaceCategory(category) ? "Y" : "N").
                        collect(Collectors.joining(","))
                );

            writer.append(LINE_SEPARATOR);
        }
    }

    private static final class SeriesRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        final boolean multiple_clubs;
        final boolean multiple_race_categories;
        final boolean multiple_races_taken_place;
        final boolean possible_to_have_completed;

        private SeriesRaceOverallResultPrinterHTML(final RaceInternal race, final OutputStreamWriter writer) {

            super(race, writer);

            final SeriesRaceResultsCalculator calculator = (SeriesRaceResultsCalculator) race.getResultsCalculator();
            multiple_clubs = countClubs(calculator.getOverallResults()) > 1;
            multiple_race_categories = calculator.getRaceCategories().size() > 1;
            multiple_races_taken_place = ((SeriesRace) race).getNumberOfRacesTakenPlace() > 1;
            possible_to_have_completed = ((SeriesRace) race).getNumberOfRacesTakenPlace() >= (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);
        }

        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");
            final List<String> headers = new ArrayList<>(common_headers);

            if (multiple_clubs)
                headers.add("Club");

            // This traverses races in order of listing in config.
            final List<SingleRaceInternal> races = ((SeriesRace) race).getRaces();

            for (final SingleRaceInternal individual_race : races)
                // Check whether race has taken place at this point.
                if (individual_race != null)
                    headers.add((String) individual_race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS));

            if (multiple_races_taken_place)
                headers.add("Total");
            if (possible_to_have_completed)
                headers.add("Completed");

            if (multiple_race_categories)
                for (final SeriesRaceCategory category : ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getRaceCategories())
                    headers.add(category.category_title());

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final SeriesRaceResultsCalculator calculator = (SeriesRaceResultsCalculator) race.getResultsCalculator();
            final SeriesRaceScorer scorer = calculator.getScorer();
            final SeriesRaceResult result = (SeriesRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            elements.add(result.getPositionString());
            elements.add(race.getNormalisation().htmlEncode(result.getParticipantName()));
            elements.add(result.getParticipant().getCategory().getShortName());
            if (multiple_clubs)
                elements.add(runner.getClub());

            for (final SingleRaceInternal individual_race : ((SeriesRace) race).getRaces())
                if (individual_race != null) {
                    final Performance score = scorer.getIndividualRacePerformance((Runner) result.getParticipant(), individual_race);
                    elements.add(renderScore(score));
                 }

            if (multiple_races_taken_place)
                elements.add(renderScore(scorer.getSeriesPerformance(runner)));
            if (possible_to_have_completed)
                elements.add(result.hasCompletedSeries() ? "Y" : "N");

            if (multiple_race_categories)
                for (final SeriesRaceCategory category : calculator.getRaceCategories())
                    elements.add(result.hasCompletedRaceCategory(category) ? "Y" : "N");

            return elements;
        }
    }

    private static final class SeriesRacePrizeResultPrinterHTML extends PrizeResultPrinterHTML {

        public SeriesRacePrizeResultPrinterHTML(final RaceInternal race, final OutputStreamWriter writer) {
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

    public static String renderScore(final Performance score) {

        return score != null ? String.valueOf(score) : "-";
    }
}
