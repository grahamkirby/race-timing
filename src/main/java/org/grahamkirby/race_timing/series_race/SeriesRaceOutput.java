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

    @Override
    protected void printPrizes() throws IOException {

        final SeriesRaceResults results = (SeriesRaceResults) race_results;

        if (results.getNumberOfRacesTakenPlace() == results.getRaceNames().size())
            super.printPrizes();
    }

    private static String renderScore(final Performance score) {

        return score != null ? String.valueOf(score) : "-";
    }

    private static final class SeriesRaceOverallResultPrinterCSV extends ResultPrinter {

        private SeriesRaceOverallResultPrinterCSV(final RaceResults race, final OutputStreamWriter writer) {

            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final SeriesRaceResults results = (SeriesRaceResults) race_results;

            final String race_names = results.getRaceNames().stream().
                filter(Objects::nonNull).
                collect(Collectors.joining(","));

            final String race_categories_header = results.getRaceCategories().stream().
                map(SeriesRaceCategory::category_title).
                collect(Collectors.joining(","));

            writer.append("Pos,Runner,");
            if (results.multipleClubs())
                writer.append("Club,");
            writer.append("Category,").
                append(race_names);
            if (results.getNumberOfRacesTakenPlace() > 1)
                writer.append(",Total");
            if (results.possibleToHaveCompleted())
                writer.append(",Completed");
            if (results.multipleRaceCategories())
                writer.append(",").append(race_categories_header);
            writer.append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SeriesRaceResults results = (SeriesRaceResults) race_results;

            final SeriesRaceResult result = (SeriesRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            writer.append(result.getPositionString()).append(",").
                append(encode(runner.getName())).append(",");
            if (results.multipleClubs())
                writer.append(encode((runner).getClub())).append(",");
            writer.append(runner.getCategory().getShortName()).append(",");

            writer.append(result.getPerformances().stream().
                map(SeriesRaceOutput::renderScore).
                collect(Collectors.joining(",")));

            if (results.getNumberOfRacesTakenPlace() > 1)
                writer.append("," ).append(renderScore(result.getPerformance()));
            if (results.possibleToHaveCompleted())
                writer.append(",").append(result.hasCompletedSeries() ? "Y" : "N");

            if (results.multipleRaceCategories())
                writer.append(",").append(
                    results.getRaceCategories().stream().
                        map(category -> result.hasCompletedRaceCategory(category) ? "Y" : "N").
                        collect(Collectors.joining(","))
                );

            writer.append(LINE_SEPARATOR);
        }
    }

    private static final class SeriesRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private SeriesRaceOverallResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {

            super(race, writer);
        }

        protected List<String> getResultsColumnHeaders() {

            final SeriesRaceResults results = (SeriesRaceResults) race_results;

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");
            final List<String> headers = new ArrayList<>(common_headers);

            if (results.multipleClubs())
                headers.add("Club");

            // This traverses race names in order of listing in config.
            results.getRaceNames().stream().
                filter(Objects::nonNull).
                forEach(headers::add);

            if (results.getNumberOfRacesTakenPlace() > 1)
                headers.add("Total");
            if (results.possibleToHaveCompleted())
                headers.add("Completed");

            if (results.multipleRaceCategories())
                for (final SeriesRaceCategory category : results.getRaceCategories())
                    headers.add(category.category_title());

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final SeriesRaceResults results = (SeriesRaceResults) race_results;
            final SeriesRaceResult result = (SeriesRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            elements.add(result.getPositionString());
            elements.add(race_results.getNormalisation().htmlEncode(result.getParticipantName()));
            elements.add(result.getParticipant().getCategory().getShortName());

            if (results.multipleClubs())
                elements.add(runner.getClub());

            result.getPerformances().forEach(
                performance -> elements.add(renderScore(performance)));

            if (results.getNumberOfRacesTakenPlace() > 1)
                elements.add(renderScore(result.getPerformance()));
            if (results.possibleToHaveCompleted())
                elements.add(result.hasCompletedSeries() ? "Y" : "N");

            if (results.multipleRaceCategories())
                for (final SeriesRaceCategory category : results.getRaceCategories())
                    elements.add(result.hasCompletedRaceCategory(category) ? "Y" : "N");

            return elements;
        }
    }

    private static final class SeriesRacePrizeResultPrinterHTML extends PrizeResultPrinterHTML {

        public SeriesRacePrizeResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).getClub();
        }

        @Override
        protected String renderPerformance(final RaceResult r) {

            return renderScore(r.getPerformance());
        }
    }
}
