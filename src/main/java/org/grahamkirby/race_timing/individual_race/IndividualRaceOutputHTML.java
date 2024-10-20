/*
 * Copyright 2024 Graham Kirby:
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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class IndividualRaceOutputHTML extends RaceOutputHTML {

    public IndividualRaceOutputHTML(final IndividualRace race) {
        super(race);
    }

    @Override
    public void printCombined() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {

            html_writer.append("""
                    <h3><strong>Results</strong></h3>
                    """);

            printPrizes(html_writer);

            html_writer.append("""
                    <h4>Overall</h4>
                    """);

            printOverallResults(html_writer, true);
        }
    }

    public void printPrizes(final OutputStreamWriter writer) throws IOException {

        writer.append("<h4>Prizes</h4>\n");

        final List<Category> categories = race.categories.getPrizeCategoriesInReportOrder();

        for (final Category category : categories)
            if (prizesInThisOrLaterCategory(category, categories)) printPrizes(writer, category);
    }

    @Override
    protected ResultPrinter getResultPrinter(OutputStreamWriter writer) {
        return new PrizeResultPrinterHTML(((IndividualRace)race), writer);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>No</th>
                                       <th>Runner</th>
                                       <th>Club</th>
                                       <th>Cat</th>
                                       <th>Time</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    protected void printOverallResultsBody(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final RaceResult r : race.getOverallResults()) {

            final IndividualRaceResult result = ((IndividualRaceResult)r);

            writer.append("""
                        <tr>
                            <td>""");
            if (!result.DNF) writer.append(String.valueOf(position++));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.entry.bib_number));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(race.normalisation.htmlEncode(result.entry.runner.name));
            writer.append("""
                            </td>
                            <td>""");
            writer.append((result.entry.runner.club));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.entry.runner.category.getShortName());
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.DNF ? DNF_STRING : format(result.duration()));
            writer.append("""
                            </td>
                        </tr>""");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record PrizeResultPrinterHTML(IndividualRace race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final IndividualRaceResult result = ((IndividualRaceResult)r);

            writer.append("<li>").
                    append(result.position_string).append(" ").
                    append(race.normalisation.htmlEncode(result.entry.runner.name)).append(" (").
                    append((result.entry.runner.club)).append(") ").
                    append(format(result.duration())).append("</li>\n");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("No results\n");
        }
    }
}

