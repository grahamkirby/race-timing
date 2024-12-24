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
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

public abstract class RaceOutputHTML extends RaceOutput {

    protected static final String SOFTWARE_CREDIT_LINK_TEXT = "<p style=\"font-size:smaller; font-style:italic;\">Results generated using <a href=\"https://github.com/grahamkirby/race-timing\">race-timing</a>.</p>";

    protected String combined_results_filename;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected RaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void printCreditLink() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(combined_results_filename + getFileSuffix()), StandardOpenOption.APPEND);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    public void printCombined() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(combined_results_filename + getFileSuffix()));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);
            writer.append(getPrizesSectionHeader());

            printPrizes(writer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            printResults(writer, getOverallResultPrinter(writer));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();
        combined_results_filename = STR."\{race_name_for_filenames}_combined_\{year}";
    }

    @Override
    public String getFileSuffix() {
        return ".html";
    }

    @Override
    public String getPrizesSectionHeader() {
        return STR."<h4>Prizes</h4>\{LINE_SEPARATOR}";
    }

    @Override
    public String getPrizeCategoryHeader(final PrizeCategory category) {
        return STR."""
        <p><strong>\{category.getLongName()}</strong></p>
        """;
    }

    @Override
    public String getPrizeCategoryFooter() {
        return "";
    }

    @Override
    public String makeSubHeading(final String s) {
        return STR."""
            <h4>\{s}</h4>
            """;
    }

    @Override
    public String getResultsHeader() {
        return "";
    }
}
