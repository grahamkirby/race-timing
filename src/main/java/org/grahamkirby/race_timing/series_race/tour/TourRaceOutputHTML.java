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
import org.grahamkirby.race_timing.individual_race.TimedRaceResult;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("""
                <table class="fac-table">
                    <thead>
                        <tr>
                            <th>Pos</th>
                            <th>Runner</th>
                            <th>Cat</th>
                            <th>Club</th>
                """);

            final List<SingleRace> races = ((SeriesRace) race).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
                    writer.append(STR."            <th>Race \{String.valueOf(i + 1)}</th>\n");

            writer.append("""
                            <th>Total</th>
                        </tr>
                    </thead>
                    <tbody>
                """);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final TourRaceResult result = (TourRaceResult) r;
            final TourRace race = (TourRace) result.race;
            final List<SingleRace> races = race.getRaces();

            writer.append(STR."""
                    <tr>
                        <td>\{result.shouldDisplayPosition() ? result.position_string : "-"}</td>
                        <td>\{race.normalisation.htmlEncode(result.runner.name)}</td>
                        <td>\{result.runner.category.getShortName()}</td>
                        <td>\{result.runner.club}</td>
            """);

            for (int i = 0; i < result.times.size(); i++)

                if (result.times.get(i) != null)
                    writer.append(STR."            <td>\{format(result.times.get(i))}</td>\n");
                else if (races.get(i) != null)
                    writer.append("            <td>-</td>").append(LINE_SEPARATOR);

            writer.append(STR."""
                        <td>\{result.shouldDisplayPosition() ? format(result.duration()) : "-"}</td>
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

            writer.append(STR."""

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

            final TimedRaceResult result = (TimedRaceResult) r;

            writer.append(STR."""
                    <tr>
                        <td>\{result.canComplete() ? result.position_string : ""}</td>
                        <td>\{result.entry.bib_number}</td>
                        <td>\{race.normalisation.htmlEncode(result.entry.participant.name)}</td>
                        <td>\{result.entry.participant.category.getShortName()}</td>
                        <td>\{result.canComplete() ? format(result.duration()) : DNF_STRING}</td>
                    </tr>
            """);
        }
    }
}
