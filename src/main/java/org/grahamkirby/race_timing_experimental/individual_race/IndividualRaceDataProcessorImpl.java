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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.individual_race.TimedRaceInput;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;
import org.grahamkirby.race_timing_experimental.common.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Normalisation.KEY_ENTRY_COLUMN_MAP;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.single_race.SingleRaceInput.stripComment;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_ENTRIES_PATH;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_RAW_RESULTS_PATH;

public class IndividualRaceDataProcessorImpl implements RaceDataProcessor {

    private static final int NUMBER_OF_ENTRY_COLUMNS = 4;
    private Race race;

    @Override
    public void setRace(Race race) {
        this.race = race;
    }

    public String dnf_string;

    @Override
    public RaceData getRaceData() {

        Path raw_results_path = (Path) race.getConfig().get(KEY_RAW_RESULTS_PATH);
        Path entries_path = (Path) race.getConfig().get(KEY_ENTRIES_PATH);

        validateRawResults(raw_results_path);
        validateEntriesNumberOfElements(entries_path);
        validateEntryCategories(entries_path);
        validateBibNumbersUnique(entries_path);

        try {
            List<RawResult> raw_results = loadRawResults(raw_results_path);
            List<RaceEntry> entries = loadEntries(entries_path);

            validateEntriesUnique(entries, entries_path);
            validateBibNumbersHaveCorrespondingEntry(raw_results, entries, raw_results_path);

            return new RaceDataImpl(raw_results, entries);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<RawResult> loadRawResults(final Path raw_results_path) throws IOException {

        return Files.readAllLines(raw_results_path).stream().
            map(SingleRaceInput::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(this::makeRawResult).
            toList();
    }

    protected RawResult makeRawResult(final String line) {

        return new RawResult(line);
    }
    private void validateBibNumbersUnique(final Path entries_path) {

        try {
            final Set<String> seen = new HashSet<>();

            Files.readAllLines(entries_path).stream().
                map(IndividualRaceDataProcessorImpl::getBibNumber).
                filter(bib_number -> !seen.add(bib_number)).
                forEach(bib_number -> {throw new RuntimeException(STR."duplicate bib number '\{bib_number}' in file '\{entries_path.getFileName()}'");});

        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }

    private void validateRawResults(final Path raw_results_path) {

        try {
            Duration previous_time = null;
            final Path raw_results_filename = raw_results_path.getFileName();

            final Set<Integer> bib_numbers_seen = new HashSet<>();

            int i = 1;
            for (final String line : Files.readAllLines(raw_results_path)) {

                final String result_string = stripComment(line);

                if (!result_string.isBlank()) {

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

    private void validateEntriesNumberOfElements(final Path entries_path) {

        final String entry_column_map_string = (String) race.getConfig().get(KEY_ENTRY_COLUMN_MAP);
        final int number_of_columns = entry_column_map_string == null ? NUMBER_OF_ENTRY_COLUMNS : entry_column_map_string.split("[,\\-]").length;

        try {
            final AtomicInteger counter = new AtomicInteger(0);

            Files.readAllLines(entries_path).stream().
                map(SingleRaceInput::stripEntryComment).
                filter(Predicate.not(String::isBlank)).
                forEach(line -> {
                    counter.incrementAndGet();
                    if (line.split("\t").length != number_of_columns)
                        throw new RuntimeException(STR."invalid entry '\{line}' at line \{counter.get()} in file '\{entries_path.getFileName()}'");
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."unexpected invalid file: '\{entries_path}'");
        }
    }

    protected void validateBibNumbersHaveCorrespondingEntry(List<RawResult> raw_results, List<RaceEntry> entries, Path raw_results_path) {

        final Set<Integer> entered_bib_numbers = entries.stream().
            map(entry -> entry.bib_number).
            collect(Collectors.toSet());

        raw_results.forEach(result -> {
                final int bib_number = result.getBibNumber();
                if (!entered_bib_numbers.contains(bib_number))
                    throw new RuntimeException(STR."invalid bib number '\{bib_number}' in file '\{raw_results_path.getFileName()}'");
            });
    }

    private void validateEntriesUnique(final List<RaceEntry> entries, Path entries_path) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1.participant != entry2.participant && entry1.participant.equals(entry2.participant))
                    throw new RuntimeException(STR."duplicate entry '\{entry1}' in file '\{entries_path.getFileName()}'");
    }

    private void validateEntryCategories(Path entries_path) {

        try {
            final AtomicInteger counter = new AtomicInteger(0);

            Files.readAllLines(entries_path).stream().
                map(SingleRaceInput::stripEntryComment).
                filter(Predicate.not(String::isBlank)).
                forEach(line -> {
                    try {
                        counter.incrementAndGet();
                        new RaceEntry(Arrays.stream(line.split("\t")).toList(), race);
                    } catch (final RuntimeException e) {
                        throw new RuntimeException(STR."invalid entry '\{e.getMessage()}' at line \{counter.get()} in file '\{entries_path.getFileName()}'", e);
                    }
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }

    private static String getBibNumber(final String line){
        return line.split("\t")[0];
    }

    List<RaceEntry> loadEntries(Path entries_path) throws IOException {

        return Files.readAllLines(entries_path).stream().
            map(SingleRaceInput::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> new RaceEntry(Arrays.stream(line.split("\t")).toList(), race)).
            toList();
    }
}
