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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Normalisation.KEY_ENTRY_COLUMN_MAP;
import static org.grahamkirby.race_timing.common.Race.COMMENT_SYMBOL;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_RAW_RESULTS_PATH;

@SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion"})
public abstract class TimedRaceInput extends SingleRaceInput {

    // Configuration file keys.
    public static final String KEY_ENTRIES_PATH = "ENTRIES_PATH";

    protected String entries_path, raw_results_path;

    protected TimedRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected void readProperties() {

        entries_path = race.getOptionalProperty(KEY_ENTRIES_PATH);
        raw_results_path = race.getOptionalProperty(KEY_RAW_RESULTS_PATH);
    }

    @Override
    public void validateInputFiles() {

        super.validateInputFiles();

        checkConfig();
        checkResultsContainValidBibNumbers();
    }

    protected abstract void checkConfig();

    protected int getNumberOfEntryColumns() {
        return 4;
    }

    protected List<RawResult> loadRawResults(final String raw_results_path) throws IOException {

        if (raw_results_path == null) return new ArrayList<>();

        final List<String> lines = Files.readAllLines(race.getPath(raw_results_path));
        final List<RawResult> raw_results = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = null;
            try {
                s = stripComment(lines.get(i));
                if (!s.isBlank()) {
                    raw_results.add(makeRawResult(s));
                }
            } catch (final RuntimeException _) {
                throw new RuntimeException(STR."invalid record '\{s}' at line \{i + 1} in file '\{raw_results_path}'");
            }
        }

        assertCorrectlyOrdered(raw_results);

        return raw_results;
    }

    public List<RawResult> loadRawResults() throws IOException {

        return loadRawResults(raw_results_path);
    }

    protected RawResult makeRawResult(final String line) {

        return new RawResult(line);
    }

    private static String stripComment(final String line) {

        final int comment_start_index = line.indexOf(COMMENT_SYMBOL);
        return (comment_start_index > -1) ? line.substring(0, comment_start_index) : line;
    }

    private void assertCorrectlyOrdered(final List<? extends RawResult> raw_results) {

        for (int i = 0; i < raw_results.size() - 1; i++) {

            final RawResult result1 = raw_results.get(i);
            final RawResult result2 = raw_results.get(i + 1);

            if (areResultsOutOfOrder(result1, result2))
                throw new RuntimeException(STR."result out of order at line \{i + 2} in file '\{Paths.get(raw_results_path).getFileName()}'");
        }
    }

    private static boolean areResultsOutOfOrder(final RawResult result1, final RawResult result2) {

        return result1.getRecordedFinishTime() != null &&
            result2.getRecordedFinishTime() != null &&
            result1.getRecordedFinishTime().compareTo(result2.getRecordedFinishTime()) > 0;
    }

    protected void checkEntries() {

        checkEntriesFileExists();
        checkEntryNumberOfElements();
        checkEntryCategories();
        checkDuplicateBibNumbers();
    }

    private void checkDuplicateBibNumbers() {

        try {
            final List<String> lines = Files.readAllLines(race.getPath(entries_path));

            final Set<String> seen = new HashSet<>();
            lines.stream().
                map(line -> line.split("\t")[0]).
                filter(bib_number -> !seen.add(bib_number)).
                forEach(bib_number -> {throw new RuntimeException(STR."duplicate bib number '\{bib_number}' in file '\{entries_path}'");});

        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }

    private void checkEntriesFileExists() {

        if (!Files.exists(race.getPath(entries_path)))
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
    }

    private void checkEntryNumberOfElements() {

        final String entry_column_map_string = race.getOptionalProperty(KEY_ENTRY_COLUMN_MAP);
        final int number_of_columns = entry_column_map_string == null ? getNumberOfEntryColumns() : entry_column_map_string.split("[,\\-]").length;

        try {
            final List<String> lines = Files.readAllLines(race.getPath(entries_path));

            final AtomicInteger counter = new AtomicInteger(0);

            lines.stream().
                filter(Predicate.not(String::isBlank)).
                filter(s -> !s.startsWith(COMMENT_SYMBOL)).
                forEach(line -> {
                    counter.incrementAndGet();
                    if (line.split("\t").length != number_of_columns)
                        throw new RuntimeException(STR."invalid entry '\{line}' at line \{counter.get()} in file '\{entries_path}'");
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."unexpected invalid file: '\{entries_path}'");
        }
    }

    private void checkEntryCategories() {

        try {
            final List<String> lines = Files.readAllLines(race.getPath(entries_path));

            final AtomicInteger counter = new AtomicInteger(0);

            lines.stream().
                filter(Predicate.not(String::isBlank)).
                filter(s -> !s.startsWith(COMMENT_SYMBOL)).
                forEach(line -> {
                    try {
                        counter.incrementAndGet();
                        makeRaceEntry(Arrays.stream(line.split("\t")).toList());
                    } catch (final RuntimeException e) {
                        throw new RuntimeException(STR."invalid entry '\{e.getMessage()}' at line \{counter.get()} in file '\{entries_path}'", e);
                    }
                });
        } catch (final IOException _) {
            throw new RuntimeException(STR."invalid file: '\{entries_path}'");
        }
    }

    List<TimedRaceEntry> loadEntries() throws IOException {

        if (entries_path == null) return new ArrayList<>();

        final List<TimedRaceEntry> entries = Files.readAllLines(race.getPath(entries_path)).stream().
            filter(Predicate.not(String::isBlank)).
            filter(s -> !s.startsWith(COMMENT_SYMBOL)).
            map(line -> (TimedRaceEntry)makeRaceEntry(Arrays.stream(line.split("\t")).toList())).
            toList();

        assertNoDuplicateEntries(entries);

        return entries;
    }

    private void assertNoDuplicateEntries(final Iterable<? extends SingleRaceEntry> entries) {

        for (final SingleRaceEntry entry1 : entries)
            for (final SingleRaceEntry entry2 : entries)
                if (entry1 != entry2 && entry1.equals(entry2))
                    throw new RuntimeException(STR."duplicate entry '\{entry1}' in file '\{Paths.get(entries_path).getFileName()}'");
    }

    protected void checkResultsContainValidBibNumbers() {

        if (entries_path != null && raw_results_path != null)
            try {
                final List<String> entries = Files.readAllLines(race.getPath(entries_path));
                final List<String> results = Files.readAllLines(race.getPath(raw_results_path));

                final Set<String> entered_bib_numbers = entries.stream().
                    map(line -> line.split("\t")[0]).
                    collect(Collectors.toSet());

                for (final String result : results) {
                    if (!result.isEmpty() && !result.startsWith(COMMENT_SYMBOL)) {
                        final String bib_number = result.split("\t")[0];
                        if (!bib_number.equals("?") && !entered_bib_numbers.contains(bib_number))
                            throw new RuntimeException(STR."invalid bib number '\{bib_number}' in file '\{raw_results_path}'");
                    }
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
    }
}
