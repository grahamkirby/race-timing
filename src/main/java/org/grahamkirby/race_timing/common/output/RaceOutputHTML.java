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
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;

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

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + getFileSuffix()));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printResults(writer, true);
        }
    }

    public String getFileSuffix() {
        return ".html";
    }

    public String getPrizesSectionHeader() {
        return "<h4>Prizes</h4>\n";
    }

    public String getPrizesCategoryHeader(final PrizeCategory category) {
        return "<p><strong>" + category.getLongName() + "</strong></p>\n";
    }

    public String getPrizesCategoryFooter() {
        return "";
    }

    protected void printResults(final OutputStreamWriter writer, final boolean include_credit_link) throws IOException {

        int group_number = 0;
        for (final PrizeCategoryGroup group : race.prize_category_groups) {

            final String group_title = group.combined_categories_title();
            final List<PrizeCategory> prize_categories = group.categories();

            printResults(writer, prize_categories, group_title, race.prize_category_groups.size() > 1, include_credit_link && group_number++ == race.prize_category_groups.size() - 1);
        }
    }

    protected void printResults(final OutputStreamWriter writer, final List<PrizeCategory> prize_categories, final String sub_heading, boolean include_sub_heading, boolean include_credit_link) throws IOException {

        if (include_sub_heading) writer.append("<h4>").append(sub_heading).append("</h4>\n");

        final List<RaceResult> results = race.getOverallResultsByCategory(prize_categories);

        setPositionStrings(results, race.allowEqualPositions());
        getOverallResultPrinter(writer).print(results, include_credit_link);
    }

    public abstract void printCombined() throws IOException;
}
