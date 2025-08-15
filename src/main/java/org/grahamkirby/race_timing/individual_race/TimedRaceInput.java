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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.Race.*;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

public abstract class TimedRaceInput extends SingleRaceInput {

    protected String entries_path, raw_results_path;

    protected TimedRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected void readProperties() {

        entries_path = race.getRequiredProperty(KEY_ENTRIES_PATH);
        raw_results_path = race.getRequiredProperty(KEY_RAW_RESULTS_PATH);
    }

    @Override
    public void validateInputFiles() {

        super.validateInputFiles();

        validateConfig();
        validateRawResults(raw_results_path);
    }

    @Override
    protected void validateRequiredPropertiesPresent() {

        super.validateRequiredPropertiesPresent();

        race.getRequiredProperty(KEY_ENTRIES_PATH);
        race.getRequiredProperty(KEY_RAW_RESULTS_PATH);
    }

    @Override
    protected void validateEntries() {

        validateEntriesFilePresent();
        validateEntriesNumberOfElements();
        validateEntryCategories();
        validateBibNumbersUnique();
        validateBibNumbersHaveCorrespondingEntry();
    }

    protected int getNumberOfEntryColumns() {
        return 4;
    }

    private void validateRawResults(final String raw_results_path) {

        try {
            Duration previous_time = null;
            final Path raw_results_filename = Path.of(raw_results_path).getFileName();

            final Set<Integer> bib_numbers_seen = new HashSet<>();

            int i = 1;
            for (final String line : Files.readAllLines(race.getPath(raw_results_path))) {

                final String result_string = stripComment(line);

                if (!result_string.isBlank()) {
                    if (race.areRecordedBibNumbersUnique()) {
                        final int bib_number;
                        try {
                            final String bib_number_as_string = result_string.split("\t")[0];
                            bib_number = Integer.parseInt(bib_number_as_string);
                            if (bib_numbers_seen.contains(bib_number))
                                throw new RuntimeException(STR."duplicate bib number '\{bib_number}' at line \{i} in file '\{raw_results_filename}'");
                            bib_numbers_seen.add(bib_number);

                        } catch (final NumberFormatException _) {
                            throw new RuntimeException(STR."invalid record '\{result_string}' at line \{i} in file '\{raw_results_filename}'");
                        }
                    }

                    final Duration finish_time;
                    try {
                        final String time_as_string = result_string.split("\t")[1];
                        finish_time = time_as_string.equals("?") ? null : parseTime(time_as_string);

                    } catch (final RuntimeException _) {
                        throw new RuntimeException(STR."invalid record '\{result_string}' at line \{i} in file '\{raw_results_filename}'");
                    }

                    if (finish_time != null && previous_time != null && previous_time.compareTo(finish_time) > 0) {
                        throw new RuntimeException(STR."result out of order at line \{i} in file '\{raw_results_filename}'");
                    }

                    previous_time = finish_time;
                }
                i++;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<RawResult> loadRawResults(final String raw_results_path) throws IOException {

        return Files.readAllLines(race.getPath(raw_results_path)).stream().
            map(SingleRaceInput::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(this::makeRawResult).
            toList();
    }

    public List<RawResult> loadRawResults() throws IOException {

        return loadRawResults(raw_results_path);
    }

    protected RawResult makeRawResult(final String line) {

        return new RawResult(line);
    }

    private static String getBibNumber(final String line){
        return line.split("\t")[0];
    }

    private void validateEntriesFilePresent() {

        if (!Files.exists(race.getPath(entries_path)))
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
    }

    private void validateEntriesNumberOfElements() {

        final String entry_column_map_string = race.getOptionalProperty(KEY_ENTRY_COLUMN_MAP);
        final int number_of_columns = entry_column_map_string == null ? getNumberOfEntryColumns() : entry_column_map_string.split("[,\\-]").length;

        try {
            final AtomicInteger counter = new AtomicInteger(0);

            Files.readAllLines(race.getPath(entries_path)).stream().
                map(SingleRaceInput::stripEntryComment).
                filter(Predicate.not(String::isBlank)).
                forEach(line -> {
                    counter.incrementAndGet();
                    if (line.split("\t").length < number_of_columns)
                        throw new RuntimeException(STR."invalid entry '\{line}' at line \{counter.get()} in file '\{entries_path}'");
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."unexpected invalid file: '\{entries_path}'");
        }
    }

    private void validateEntryCategories() {

        try {
            final AtomicInteger counter = new AtomicInteger(0);

            Files.readAllLines(race.getPath(entries_path)).stream().
                map(SingleRaceInput::stripEntryComment).
                filter(Predicate.not(String::isBlank)).
                forEach(line -> {
                    try {
                        counter.incrementAndGet();
                        makeRaceEntry(Arrays.stream(line.split("\t")).toList());
                    } catch (final RuntimeException e) {
                        throw new RuntimeException(STR."invalid category in entry '\{e.getMessage()}' at line \{counter.get()} in file '\{entries_path}'", e);
                    }
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }

    List<SingleRaceEntry> loadEntries() throws IOException {

        final List<SingleRaceEntry> entries = Files.readAllLines(race.getPath(entries_path)).stream().
            map(SingleRaceInput::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> makeRaceEntry(Arrays.stream(line.split("\t")).toList())).
            toList();

        // TODO move validation to separate earlier check.
        validateEntriesUnique(entries);

        return entries;
    }

    private void validateEntriesUnique(final Iterable<? extends SingleRaceEntry> entries) {

        for (final SingleRaceEntry entry1 : entries)
            for (final SingleRaceEntry entry2 : entries)
                if (entry1 != entry2 && entry1.equals(entry2))
                    throw new RuntimeException(STR."duplicate entry '\{entry1}' in file '\{Path.of(entries_path).getFileName()}'");
    }

    protected void validateBibNumbersHaveCorrespondingEntry() {

        try {
            final Set<String> entered_bib_numbers = Files.readAllLines(race.getPath(entries_path)).stream().
                map(line -> line.split("\t")[0]).
                collect(Collectors.toSet());

            AtomicInteger counter = new AtomicInteger(0);
            Files.readAllLines(race.getPath(raw_results_path)).stream().
                peek(_ -> counter.incrementAndGet()).
                map(SingleRaceInput::stripComment).
                filter(Predicate.not(String::isBlank)).
                forEach(line -> {
                    final String bib_number = line.split("\t")[0];
                    if (!bib_number.equals("?") && !entered_bib_numbers.contains(bib_number))
                        throw new RuntimeException(STR."unregistered bib number '\{bib_number}' at line \{counter.get()} in file '\{raw_results_path}'");
                });
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private void validateBibNumbersUnique() {

        try {
            final Set<String> seen = new HashSet<>();

            AtomicInteger counter = new AtomicInteger(0);

            Files.readAllLines(race.getPath(entries_path)).stream().
                peek(_ -> counter.incrementAndGet()).
                map(TimedRaceInput::getBibNumber).
                filter(bib_number -> !seen.add(bib_number)).
                forEach(bib_number -> {throw new RuntimeException(STR."duplicate bib number '\{bib_number}' at line \{counter.get()} in file '\{entries_path}'");});

        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }
}
