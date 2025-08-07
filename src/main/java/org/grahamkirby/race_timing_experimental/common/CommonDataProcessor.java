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
package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.single_race.SingleRaceInput.stripComment;

public class CommonDataProcessor {

    public static void validateRawResults(final Path raw_results_path) throws IOException {

        final AtomicInteger line_number = new AtomicInteger(0);

        readAllLines(raw_results_path).stream().
            peek(_ -> line_number.incrementAndGet()).
            map(SingleRaceInput::stripComment).
            filter(Predicate.not(String::isBlank)).
            forEach(line -> {
                try {
                    new RawResult(line);
                } catch (final Exception _) {
                    throw new RuntimeException(STR."invalid record '\{line}' at line \{line_number} in file '\{raw_results_path.getFileName()}'");
                }
            });
    }

    public static void validateEntriesNumberOfElements(final Path entries_path, final int number_of_entry_columns, final String entry_column_map_string) throws IOException {

        final int number_of_columns = entry_column_map_string == null ? number_of_entry_columns : entry_column_map_string.split("[,\\-]").length;
        final AtomicInteger line_number = new AtomicInteger(0);

        readAllLines(entries_path).stream().
            peek(_ -> line_number.incrementAndGet()).
            map(SingleRaceInput::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            filter(line -> line.split("\t").length != number_of_columns).
            forEach(line -> {
                throw new RuntimeException(STR."invalid entry '\{line}' at line \{line_number.get()} in file '\{entries_path.getFileName()}'");
            });
    }

    public static void validateRawResultsOrdering(final Path raw_results_path) throws IOException {

        Duration previous_time = null;
        int i = 1;

        for (final String line : readAllLines(raw_results_path)) {

            final String result_string = stripComment(line);

            if (!result_string.isBlank()) {

                final String time_as_string = result_string.split("\t")[1];
                final Duration finish_time = time_as_string.equals("?") ? null : parseTime(time_as_string);

                if (finish_time != null && previous_time != null && previous_time.compareTo(finish_time) > 0)
                    throw new RuntimeException(STR."result out of order at line \{i} in file '\{raw_results_path.getFileName()}'");

                previous_time = finish_time;
            }
            i++;
        }
    }

    // This may be called with either a file of entries or a file of raw results;
    // in both cases each line starts with a bib number.
    public static void validateBibNumbersUnique(final Path file_path) throws IOException {

        final Set<String> seen = new HashSet<>();
        final AtomicInteger line_number = new AtomicInteger(0);

        readAllLines(file_path).stream().
            peek(_ -> line_number.incrementAndGet()).
            map(CommonDataProcessor::getBibNumber).
            filter(bib_number -> !seen.add(bib_number)).
            forEach(bib_number -> {
                throw new RuntimeException(STR."duplicate bib number '\{bib_number}' at line \{line_number.get()} in file '\{file_path.getFileName()}'");
            });
    }

    public static List<String> readAllLines(final Path path) throws IOException {

        return path == null ? List.of() : Files.readAllLines(path);
    }

    public static String getBibNumber(final String line){
        return line.split("\t")[0];
    }
}
