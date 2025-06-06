/*
 * Copyright 2025 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race.grand_prix;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_RESULTS;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

public class GrandPrixRaceOutputHTML extends SeriesRaceOutputHTML {

    // TODO add colour coding for race categories.

    GrandPrixRaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected final ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    @Override
    protected final ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        private List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");

            final List<String> headers = new ArrayList<>(common_headers);

            // This traverses races in order of listing in config, sorted first by race type and then date.
            for (final SingleRace individual_race : ((SeriesRace) race).getRaces())
                // Check whether race has taken place at this point.
                if (individual_race != null)
                    headers.add(individual_race.getRequiredProperty(KEY_RACE_NAME_FOR_RESULTS));

            headers.add("Total");
            headers.add("Completed?");

            for (final RaceCategory category : ((GrandPrixRace) race).race_categories)
                headers.add(STR."\{category.category_title()}?");

            return headers;
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("""
                <table class="fac-table">
                    <thead>
                        <tr>
                """);

            for (final String header : getResultsColumnHeaders())
                writer.append(STR."""
                                <th>\{header}</th>
                    """);

            writer.append("""
                        </tr>
                    </thead>
                    <tbody>
                """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = ((GrandPrixRaceResult) r);
            final GrandPrixRace grand_prix_race = (GrandPrixRace) race;

            writer.append(STR."""
                    <tr>
                        <td>\{result.position_string}</td>
                        <td>\{race.normalisation.htmlEncode(result.runner.name)}</td>
                        <td>\{result.runner.category.getShortName()}</td>
            """);

            for (final SingleRace individual_race : grand_prix_race.getRaces())
                if (individual_race != null) {
                    final int score = grand_prix_race.calculateRaceScore(individual_race, result.runner);
                    writer.append(STR."""
                                    <td>\{renderScore(score, "-")}</td>
                        """);
                }

            writer.append(STR."""
                            <td>\{result.totalScore()}</td>
                            <td>\{result.hasCompletedSeries() ? "Y" : "N"}</td>
                """);

            for (final RaceCategory category : ((GrandPrixRace) race).race_categories) {
                writer.append(STR."""
                            <td>\{result.hasCompletedRaceCategory(category) ? "Y" : "N"}</td>
                """);
            }

            writer.append("""
                        </tr>
                """);
        }
    }

    private static final class PrizeResultPrinter extends ResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(LINE_SEPARATOR);
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = ((GrandPrixRaceResult) r);

            writer.append(STR."""
                    <li>\{result.position_string}: \{result.runner.name} (\{result.runner.category.getShortName()}) \{result.totalScore()}</li>
                """);
        }
    }
}
