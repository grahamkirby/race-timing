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
package org.grahamkirby.race_timing.series_race.tour;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

class TourRaceOutputHTML extends SeriesRaceOutputHTML {

    TourRaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printIndividualRaces() throws IOException {

        for (int i = 1; i <= ((SeriesRace) race).getRaces().size(); i++)
            printIndividualRaceResults(i);
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printIndividualRaceResults(final int race_number) throws IOException {

        final SingleRace individual_race = ((SeriesRace) race).getRaces().get(race_number - 1);

        if (individual_race != null) {

            final OutputStream race_stream = getOutputStream(race_name_for_filenames, STR."race\{race_number}", year);

            try (final OutputStreamWriter writer = new OutputStreamWriter(race_stream)) {

                for (final PrizeCategoryGroup group : race.prize_category_groups)
                    printIndividualRaceResults(writer, individual_race, group.categories(), group.group_title());

                writer.append(SOFTWARE_CREDIT_LINK_TEXT).append(LINE_SEPARATOR);
            }
        }
    }

    private void printIndividualRaceResults(final OutputStreamWriter writer, final SingleRace individual_race, final Collection<PrizeCategory> prize_categories, final String sub_heading) throws IOException {

        final List<RaceResult> category_results = individual_race.getOverallResults(prize_categories);

        new IndividualRaceResultPrinter(race, sub_heading, writer).print(category_results);
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

            for (int i = 0; i < ((SeriesRace) race).getNumberOfRacesTakenPlace(); i++)
                headers.add(STR."Race \{i + 1}");

            headers.add("Total");

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final TourRaceResult result = (TourRaceResult) r;

            elements.add(result.position_string);
            elements.add(race.normalisation.htmlEncode(result.runner.name));
            elements.add(result.runner.category.getShortName());
            elements.add(result.runner.club);

            for (final Duration duration : result.times)
                elements.add(renderDuration(duration, "-"));

            elements.add(renderDuration(result, "-"));

            return elements;
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

            final TourRaceResult result = (TourRaceResult) r;

            writer.append(STR."    <li>\{result.position_string} \{race.normalisation.htmlEncode(result.runner.name)} (\{result.runner.club}) \{format(result.duration())}</li>\n");
        }
    }

    private static final class IndividualRaceResultPrinter extends ResultPrinterHTML {

        private final String sub_heading;

        private IndividualRaceResultPrinter(final Race race, final String sub_heading, final OutputStreamWriter writer) {

            super(race, writer);
            this.sub_heading = sub_heading;
        }

        @Override
        public void printResultsHeader() throws IOException {

            // TODO rationalise with OverallResultPrinter.
            // TODO use getResultsSubHeader().

            writer.append(STR."""

                <p></p>
                <h4>\{sub_heading}</h4>
                <table class="fac-table">
                    <thead>
                        <tr>
                            <th>Pos</th>
                            <th>No</th>
                            <th>Runner</th>
                            <th>Category</th>
                            <th>Total</th>
                        </tr>
                    </thead>
                    <tbody>
                """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;

            writer.append(STR."""
                    <tr>
                        <td>\{result.position_string}</td>
                        <td>\{result.entry.bib_number}</td>
                        <td>\{race.normalisation.htmlEncode(result.entry.participant.name)}</td>
                        <td>\{result.entry.participant.category.getShortName()}</td>
                        <td>\{renderDuration(result, DNF_STRING)}</td>
                    </tr>
            """);
        }
    }
}
