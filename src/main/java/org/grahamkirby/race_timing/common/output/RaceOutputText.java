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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class RaceOutputText extends RaceOutput {

    public RaceOutputText(Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final PrizeCategory category : race.getPrizeCategories())
                if (prizesInThisOrLaterCategory(category)) printPrizes(writer, category);
        }
    }

    @Override
    public void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) throws IOException {

        final String header = "Category: " + category.getLongName();

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        final List<RaceResult> results = race.prize_winners.get(category);

        setPositionStrings(results, true);
        printPrizes(writer, results);

        writer.append("\n\n");
    }

    @Override
    public void printNotes() throws IOException {

        if (!race.non_title_case_words.isEmpty()) {

            final List<String> words = new ArrayList<>(race.non_title_case_words);
            words.sort(String::compareTo);

            race.getNotes().append("Converted to title case: ");
            for (String word : words) race.getNotes().append(word).append(", ");
        }

        final String notes = race.getNotes().toString();

        if (!notes.isEmpty()) {

            final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(notes_filename + ".txt"));

            try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
                writer.append(notes);
            }
        }
    }
}
