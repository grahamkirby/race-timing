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
import java.io.OutputStreamWriter;
import java.util.List;

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

        final String header = STR."\{race_name_for_results} Results \{year}";
        return STR."""
            \{header}
            \{"=".repeat(header.length())}

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

    @Override
    void printTeamPrizes(final OutputStreamWriter writer) throws IOException {

        List<String> team_prizes = race.getTeamPrizes();

        if (!team_prizes.isEmpty()) {
            writer.append("Team Prizes\n");
            writer.append("-----------\n\n");

            for (String team_prize : team_prizes) {
                writer.append(team_prize);
                writer.append(LINE_SEPARATOR);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Full results not printed to text file.
    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }
}
