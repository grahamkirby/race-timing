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
package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

public abstract class RaceOutputHTML extends RaceOutput {

    public static final String SOFTWARE_CREDIT_LINK_TEXT = "<p style=\"font-size:smaller; font-style:italic;\">Results generated using <a href=\"https://github.com/grahamkirby/race-timing\">race-timing</a>.</p>";

    public RaceOutputHTML(Race race) {
        super(race);
    }

    @Override
    public void printResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + ".html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printResults(writer, true);
        }
    }

    @Override
    public void printPrizes() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(prizes_filename + ".html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printPrizes(writer);
        }
    }

    @Override
    protected void printPrizesInCategory(final OutputStreamWriter writer, final PrizeCategory category) throws IOException {

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        writer.append("<p><strong>").
                append(category.getLongName()).
                append("</strong></p>\n");

        if (!category_prize_winners.isEmpty()) {

            writer.append("<ul>\n");

            setPositionStrings(category_prize_winners, race.allowEqualPositions());
            printResults(category_prize_winners, getResultPrinter(writer));

            writer.append("</ul>\n\n");
        }
        else {
            writer.append("<p>No results</p>\n");
        }
    }

    protected void printPrizes(final OutputStreamWriter writer) throws IOException {

        writer.append("<h4>Prizes</h4>\n");

        for (Race.PrizeCategoryGroup group : race.prize_category_groups)
            if (prizesInThisOrLaterGroup(group))
                for (final PrizeCategory category : group.categories())
                    if (prizesInThisOrLaterCategory(category))
                        printPrizesInCategory(writer, category);
    }

    protected void printResults(final OutputStreamWriter writer, final boolean include_credit_link) throws IOException {

        printResultsHeader(writer);
        printResultsBody(writer);
        printResultsFooter(writer, include_credit_link);
    }

    protected void printResultsFooter(final OutputStreamWriter writer, final boolean include_credit_link) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);

        if (include_credit_link) writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    protected abstract void printResultsBody(final OutputStreamWriter writer) throws IOException;
    protected abstract void printResultsHeader(final OutputStreamWriter writer) throws IOException;
    public abstract void printCombined() throws IOException;
}
