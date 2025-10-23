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
package org.grahamkirby.race_timing.common;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.LINE_SEPARATOR;

/** Base class for printing results to HTML files. */
public abstract class OverallResultPrinterHTML extends ResultPrinter {

    protected OverallResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printResultsHeader() throws IOException {

        writer.append("""
            <table class="fac-table">
                <thead>
                    <tr>
            """);

        for (final String header : getResultsColumnHeaders())
            writer.append("            <th>"  + header + "</th>" + LINE_SEPARATOR);

        writer.append("""
                    </tr>
                </thead>
                <tbody>
            """);
    }

    @Override
    public void printResult(final RaceResult result) throws IOException {

        writer.append("""
                    <tr>
            """);

        for (final String element : getResultsElements(result))
            writer.append("            <td>" + element + "</td>" + LINE_SEPARATOR);

        writer.append("""
                    </tr>
            """);
    }

    @Override
    public void printResultsFooter() throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    @Override
    public void printNoResults() throws IOException {

        writer.append("<p>No results</p>").append(LINE_SEPARATOR);
    }

    protected abstract List<String> getResultsElements(final RaceResult result) throws IOException;
    protected abstract List<String> getResultsColumnHeaders();
}
