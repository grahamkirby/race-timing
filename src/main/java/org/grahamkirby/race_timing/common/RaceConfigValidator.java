/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Config.*;

public abstract class RaceConfigValidator {

    private static class BoxedLine {
        String line;
    }

    public static class BoxedLineNumber {
        public int line = 0;
    }

    private static class BoxedDuration {
        Duration duration;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int RAW_RESULT_TIME_INDEX = 1;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void validateRawResults(final Path raw_results_path) throws IOException {

        final BoxedLine line = new BoxedLine();
        final BoxedLineNumber line_number = new BoxedLineNumber();

        getCleanedLines(raw_results_path, line, line_number).
            forEach(cleaned_line -> validateRawResultLine(cleaned_line, line.line, raw_results_path, line_number.line));
    }

    public static void validateEntriesNumberOfElements(final Path entries_path, final String entry_column_map_string) throws IOException {

        validateEntriesNumberOfElements(entries_path, 0, entry_column_map_string);
    }

    public static void validateEntriesNumberOfElements(final Path entries_path, final int number_of_entry_columns, final String entry_column_map_string) throws IOException {

        final int min_number_of_columns = entry_column_map_string == null ?
            number_of_entry_columns :

            // Find the highest column number referenced in the column map.
            Arrays.stream(entry_column_map_string.split("[,\\-]", -1)).
            map(Integer::parseInt).
            reduce(Math::max).
            orElseThrow();

        final BoxedLine line = new BoxedLine();
        final BoxedLineNumber line_number = new BoxedLineNumber();

        getCleanedLines(entries_path, line, line_number).
            forEach(cleaned_line -> validateEntryNumberOfElements(cleaned_line, line.line, min_number_of_columns, entries_path, line_number.line));
    }

    public static void validateEntryCategories(final Path entries_path, final Consumer<String> check_category_in_line) throws IOException {

        final BoxedLineNumber line_number = new BoxedLineNumber();

        getCleanedLines(entries_path, line_number).
            forEach(line -> validateEntryCategory(line, check_category_in_line, entries_path, line_number));
    }

    public static void validateRawResultsOrdering(final Path raw_results_path) throws IOException {

        final BoxedLineNumber line_number = new BoxedLineNumber();
        final BoxedDuration previous_time = new BoxedDuration();

        getCleanedLines(raw_results_path, line_number).
            map(line -> line.split(RAW_RESULT_SEPARATOR)[RAW_RESULT_TIME_INDEX]).
            filter(time_string -> !time_string.equals(UNKNOWN_TIME_INDICATOR)).
            map(NormalisationProcessor::parseTime).
            forEachOrdered(finish_time -> validateConsecutiveRawResultsOrdering(finish_time, previous_time, raw_results_path, line_number));
    }

    // This may be called with either a file of entries or a file of raw results;
    // in both cases each line should start with a bib number.
    public static void validateBibNumbers(final Path file_path) throws IOException {

        final Set<String> seen = new HashSet<>();
        final BoxedLineNumber line_number = new BoxedLineNumber();

        getCleanedLines(file_path, line_number).
            forEach(line -> {

                final String bib_number = getBibNumber(line);

                if (!validBibNumber(bib_number))
                    throw new RuntimeException(INVALID_ENTRY + " '" + line + "' " + AT_LINE + " " + line_number.line + " " + IN_FILE + " '" + file_path.getFileName() + "'");

                if (!seen.add(bib_number))
                    throw new RuntimeException(DUPLICATE_BIB_NUMBER + " '" + bib_number + "' " + AT_LINE + " " + line_number.line + " " + IN_FILE + " '" + file_path.getFileName() + "'");
            });
    }

    public static void validateRecordedBibNumbersAreRegistered(final List<RaceEntry> entries, final Path raw_results_path) throws IOException {

        final BoxedLineNumber line_number = new BoxedLineNumber();

        final Set<Integer> entry_bib_numbers = entries.stream().
            map(RaceEntry::getBibNumber).
            collect(Collectors.toSet());

        getCleanedLines(raw_results_path, line_number).
            map(RawResult::new).
            map(RawResult::getBibNumber).
            forEach(bib_number -> validateResultBibNumberRegistered(bib_number, entry_bib_numbers, raw_results_path, line_number));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static void validateRawResultLine(final String cleaned_line, final String original_line, final Path raw_results_path, final int line_number) {

        try {
            new RawResult(cleaned_line);

        } catch (final Exception _) {
            String message = INVALID_RECORD + " '" + original_line + "' " + AT_LINE + " " + line_number + " " + IN_FILE + " '" + raw_results_path.getFileName() + "'";
            if (original_line.contains(COMMENT_SYMBOL))
                message += " - " + POSSIBLE_INVALID_USE_OF_COMMENT_SYMBOL + LINE_SEPARATOR;
            throw new RuntimeException(message);
        }
    }

    private static void validateEntryNumberOfElements(final String cleaned_line, final String original_line, final int min_number_of_columns, final Path entries_path, final int line_number) {

        if (cleaned_line.split("\t", -1).length < min_number_of_columns) {

            String message = INVALID_ENTRY + " '" + original_line + "' " + AT_LINE + " " + line_number + " " + IN_FILE + " '" + entries_path.getFileName() + "'";
            if (original_line.contains(COMMENT_SYMBOL))
                message += " - " + POSSIBLE_INVALID_USE_OF_COMMENT_SYMBOL + LINE_SEPARATOR;
            throw new RuntimeException(message);
        }
    }

    private static void validateEntryCategory(final String line, final Consumer<String> check_category_in_line, final Path entries_path, final BoxedLineNumber line_number) {

        try {
            check_category_in_line.accept(line);

        } catch (final RuntimeException e) {
            throw new RuntimeException(INVALID_CATEGORY_IN_ENTRY + " '" + e.getMessage() + "' " + AT_LINE + " " + line_number.line + " " + IN_FILE + " '" + entries_path.getFileName() + "'");
        }
    }

    private static void validateConsecutiveRawResultsOrdering(final Duration this_time, final BoxedDuration previous_time, final Path raw_results_path, final BoxedLineNumber line_number) {

        if (this_time != null && previous_time.duration != null && previous_time.duration.compareTo(this_time) > 0)
            throw new RuntimeException(AT_LINE1 + " " + line_number.line + " " + IN_FILE + " '" + raw_results_path.getFileName() + "'");

        previous_time.duration = this_time;
    }

    private static void validateResultBibNumberRegistered(final int bib_number, final Set<Integer> entry_bib_numbers, final Path raw_results_path, final BoxedLineNumber line_number) {

        if (bib_number != UNKNOWN_BIB_NUMBER && !entry_bib_numbers.contains(bib_number))
            throw new RuntimeException(UNREGISTERED_BIB_NUMBER + " '" + bib_number + "' " + AT_LINE + " " + line_number.line + " " + IN_FILE + " '" + raw_results_path.getFileName() + "'");
    }

    private static Stream<String> getCleanedLines(final Path file_path, final BoxedLineNumber line_number) throws IOException {

        return getCleanedLines(file_path, new BoxedLine(), line_number);
    }

    private static Stream<String> getCleanedLines(final Path file_path, final BoxedLine line, final BoxedLineNumber line_number) throws IOException {

        return readAllLines(file_path).stream().
            peek(_ -> line_number.line++).
            peek(l -> line.line = l).
            map(NormalisationProcessor::stripComment).
            filter(Predicate.not(String::isBlank));
    }

    private static boolean validBibNumber(final String bib_number) {
        try {
            Integer.parseInt(bib_number);
            return true;
        } catch (NumberFormatException _) {
            return false;
        }
    }

    private static String getBibNumber(final String line){
        return line.split("\t")[0];
    }
}
