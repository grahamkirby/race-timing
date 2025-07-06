/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing.common.output;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.StandardOpenOption;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

/** Base class for HTML output. */
public abstract class RaceOutputHTML extends RaceOutput {

    /** Web link to application on GitHub. */
    public static final String SOFTWARE_CREDIT_LINK_TEXT = "<p style=\"font-size:smaller; font-style:italic;\">Results generated using <a href=\"https://github.com/grahamkirby/race-timing\">race-timing</a>.</p>";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected RaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints all details to a single web page. */
    public void printCombined() throws IOException {

        printCombinedDetails();
        printCreditLink();
    }

    /** Prints prizes and overall results to a single web page. */
    protected void printCombinedDetails() throws IOException {

        final OutputStream stream = getOutputStream(race_name_for_filenames, "combined", year);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(getPrizesHeader());
            printPrizes(writer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            printResults(writer, getOverallResultPrinter(writer));
        }
    }

    /** Prints web link to GitHub page for this application. */
    private void printCreditLink() throws IOException {

        final OutputStream stream = getOutputStream(race_name_for_filenames, "combined", year, StandardOpenOption.APPEND);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getFileSuffix() {
        return ".html";
    }

    @Override
    public String getResultsHeader() {
        return "";
    }

    @Override
    public String getResultsSubHeader(final String s) {
        return STR."""
            <p></p>
            <h4>\{s}</h4>
            """;
    }

    @Override
    public String getPrizesHeader() {
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
}
