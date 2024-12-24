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

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

public abstract class RaceOutputText extends RaceOutput {

    protected RaceOutputText(final Race race) {
        super(race);
    }

    @Override
    protected String getFileSuffix() {
        return ".txt";
    }

    @Override
    protected String getPrizesSectionHeader() {
        return STR."""
            \{race_name_for_results} Results \{year}
            ============================

            """;
    }

    @Override
    protected String getResultsHeader() {
        return "";
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

    public void printNotes() throws IOException {

        race.non_title_case_words.stream().
            sorted().
            reduce((s1, s2) -> STR."\{s1}, \{s2}").
            ifPresent(s -> race.getNotes().append("Converted to title case: ").append(s));

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(notes_filename + getFileSuffix()));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
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
