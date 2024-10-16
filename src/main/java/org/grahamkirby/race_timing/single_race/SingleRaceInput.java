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
package org.grahamkirby.race_timing.single_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RawResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.KEY_ENTRIES_FILENAME;
import static org.grahamkirby.race_timing.common.Race.KEY_RAW_RESULTS_FILENAME;

public abstract class SingleRaceInput extends RaceInput {

    public SingleRaceInput(final Race race) {

        super(race);

        readProperties();
        constructFilePaths();
    }

    protected void readProperties() {

        entries_filename = race.getProperties().getProperty(KEY_ENTRIES_FILENAME);
        raw_results_filename = race.getProperties().getProperty(KEY_RAW_RESULTS_FILENAME);
    }

    protected void constructFilePaths() {

        input_directory_path = race.getWorkingDirectoryPath().resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);
    }

    protected List<RaceEntry> loadEntries() throws IOException {

        final Function<String, RaceEntry> race_entry_mapper = line -> makeRaceEntry(Arrays.stream(line.split("\t")).toList());
        final List<RaceEntry> entries = Files.readAllLines(entries_path).stream().map(race_entry_mapper).toList();

        checkForDuplicateBibNumbers(entries);
        checkForDuplicateEntries(entries);

        return entries;
    }

    protected List<RawResult> loadRawResults(final Path results_path) throws IOException {

        final List<RawResult> raw_results = new ArrayList<>();

        for (final String line : Files.readAllLines(results_path))
            addResult(stripComment(line), raw_results);

        checkOrdering(raw_results);

        return raw_results;
    }

    private void addResult(final String line, final List<RawResult> raw_results) {

        if (!line.isBlank())
            try {
                raw_results.add(loadRawResult(line));

            } catch (final NumberFormatException ignored) {
            }
    }

    protected RawResult loadRawResult(final String line) {

        return new RawResult(line);
    }

    protected static String stripComment(final String line) {

        final int comment_start_index = line.indexOf(Race.COMMENT_SYMBOL);
        return (comment_start_index > -1) ? line.substring(0, comment_start_index) : line;
    }

    private void checkOrdering(final List<RawResult> raw_results) {

        for (int i = 0; i < raw_results.size() - 1; i++) {

            final RawResult result1 = raw_results.get(i);
            final RawResult result2 = raw_results.get(i+1);

            if (resultsAreOutOfOrder(result1, result2))
                throw new RuntimeException("result " + (i+2) + " out of order");
        }
    }

    private boolean resultsAreOutOfOrder(final RawResult result1, final RawResult result2) {

        return result2.getRecordedFinishTime() != null &&
                result1 != null && result1.getRecordedFinishTime() != null &&
                result1.getRecordedFinishTime().compareTo(result2.getRecordedFinishTime()) > 0;
    }

    private void checkDuplicateBibNumber(final List<RaceEntry> entries, final RaceEntry new_entry) {

        for (final RaceEntry entry : entries)
            if (entry != null && entry.bib_number == new_entry.bib_number)
                throw new RuntimeException("duplicate bib number: " + new_entry.bib_number);
    }

    protected void checkForDuplicateBibNumbers(final List<RaceEntry> entries) {

        for (final RaceEntry entry1 : entries) {
            for (final RaceEntry entry2 : entries) {

                if (entry1 != entry2 && entry1.bib_number == entry2.bib_number)
                    throw new RuntimeException("duplicate bib number: " + entry1.bib_number);
            }
        }
    }

    protected abstract List<RawResult> loadRawResults() throws IOException;
    protected abstract void checkForDuplicateEntries(final List<RaceEntry> entries);
    protected abstract RaceEntry makeRaceEntry(final List<String> elements);
}
