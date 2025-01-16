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
package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

/** Base class for plaintext output. */
public abstract class RaceOutputText extends RaceOutput {

    protected RaceOutputText(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getFileSuffix() {
        return ".txt";
    }

    /** No headings in plaintext file. */
    @Override
    protected String getResultsHeader() {
        return "";
    }

    @Override
    protected String getPrizesHeader() {
        return STR."""
            \{race_name_for_results} Results \{year}
            ============================

            """;
    }

    @Override
    protected String getPrizeCategoryHeader(final PrizeCategory category) {

        final String header = STR."Category: \{category.getLongName()}";
        return STR."""
            \{header}
            \{"-".repeat(header.length())}

            """;
    }

    @Override
    protected String getPrizeCategoryFooter() {
        return LINE_SEPARATOR + LINE_SEPARATOR;
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    public void printNotes() throws IOException {

        final String converted_words = race.normalisation.getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.getNotes().append("Converted to title case: ").append(converted_words);

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race_name_for_filenames, "processing_notes", year))) {
            writer.append(race.getNotes().toString());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Full results not printed to text file.
    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }
}
