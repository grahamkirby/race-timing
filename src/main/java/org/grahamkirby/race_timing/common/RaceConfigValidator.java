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
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Config.*;

public class RaceConfigValidator extends ConfigProcessor {

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

    public static final List<String> REQUIRED_CONFIG_KEYS = List.of(

        Config.KEY_YEAR,
        Config.KEY_RACE_NAME_FOR_FILENAMES,
        Config.KEY_RACE_NAME_FOR_RESULTS,
        Config.KEY_ENTRY_CATEGORIES_PATH,
        Config.KEY_PRIZE_CATEGORIES_PATH);

    private static final int RAW_RESULT_TIME_INDEX = 1;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RaceConfigValidator(final Config config) {

        super(config);
    }

    public void processConfig() {

        checkAllPresent(REQUIRED_CONFIG_KEYS);

        checkAllFilesExist(List.of(
            KEY_ENTRY_CATEGORIES_PATH,
            KEY_PRIZE_CATEGORIES_PATH));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void validateRawResults(final Path raw_results_path) throws IOException {

        final BoxedLine line = new BoxedLine();
        final BoxedLineNumber line_number = new BoxedLineNumber();

        getCleanedLines(raw_results_path, line, line_number).
            forEach(cleaned_line -> validateRawResultLine(cleaned_line, line.line, raw_results_path, line_number.line));
    }

    public static void validateEntriesNumberOfElements(final Path entries_path, final int number_of_entry_columns, final String entry_column_map_string) throws IOException {

        final int number_of_columns = entry_column_map_string == null ?
            number_of_entry_columns :
            entry_column_map_string.split("[,\\-]").length;

        final BoxedLine line = new BoxedLine();
        final BoxedLineNumber line_number = new BoxedLineNumber();

        getCleanedLines(entries_path, line, line_number).
            forEach(cleaned_line -> validateEntryNumberOfElements(cleaned_line, line.line, number_of_columns, entries_path, line_number.line));
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
            map(line -> line.split("\t")[RAW_RESULT_TIME_INDEX]).
            filter(time_string -> !time_string.equals(UNKNOWN_TIME_INDICATOR)).
            map(Normalisation::parseTime).
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
                    throw new RuntimeException("invalid entry '" + line + "' at line " + line_number.line + " in file '" + file_path.getFileName() + "'");

                if (!seen.add(bib_number))
                    throw new RuntimeException("duplicate bib number '" + bib_number + "' at line " + line_number.line + " in file '" + file_path.getFileName() + "'");
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
            String message = "invalid record '" + original_line + "' at line " + line_number + " in file '" + raw_results_path.getFileName() + "'";
            if (original_line.contains(COMMENT_SYMBOL))
                message += " - possible invalid use of # comment symbol";
            throw new RuntimeException(message);
        }
    }

    private static void validateEntryNumberOfElements(final String cleaned_line, final String original_line, final int number_of_columns, final Path entries_path, final int line_number) {

        if (cleaned_line.split("\t").length < number_of_columns) {
            String message = "invalid entry '" + original_line + "' at line " + line_number + " in file '" + entries_path.getFileName() + "'";
            if (original_line.contains(COMMENT_SYMBOL))
                message += " - possible invalid use of # comment symbol";
            throw new RuntimeException(message);
        }
    }

    private static void validateEntryCategory(final String line, final Consumer<String> check_category_in_line, final Path entries_path, final BoxedLineNumber line_number) {

        try {
            check_category_in_line.accept(line);

        } catch (final RuntimeException e) {
            throw new RuntimeException("invalid category in entry '" + e.getMessage() + "' at line " + line_number.line + " in file '" + entries_path.getFileName() + "'", e);
        }
    }

    private static void validateConsecutiveRawResultsOrdering(final Duration this_time, final BoxedDuration previous_time, final Path raw_results_path, final BoxedLineNumber line_number) {

        if (this_time != null && previous_time.duration != null && previous_time.duration.compareTo(this_time) > 0)
            throw new RuntimeException("result out of order at line " + line_number.line + " in file '" + raw_results_path.getFileName() + "'");

        previous_time.duration = this_time;
    }

    private static void validateResultBibNumberRegistered(final int bib_number, final Set<Integer> entry_bib_numbers, final Path raw_results_path, final BoxedLineNumber line_number) {

        if (bib_number != UNKNOWN_BIB_NUMBER && !entry_bib_numbers.contains(bib_number))
            throw new RuntimeException("unregistered bib number '" + bib_number + "' at line " + line_number.line + " in file '" + raw_results_path.getFileName() + "'");
    }

    private static Stream<String> getCleanedLines(final Path file_path, final BoxedLineNumber line_number) throws IOException {

        return getCleanedLines(file_path, new BoxedLine(), line_number);
    }

    private static Stream<String> getCleanedLines(final Path file_path, final BoxedLine line, final BoxedLineNumber line_number) throws IOException {

        return readAllLines(file_path).stream().
            peek(_ -> line_number.line++).
            peek(l -> line.line = l).
            map(Normalisation::stripComment).
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
