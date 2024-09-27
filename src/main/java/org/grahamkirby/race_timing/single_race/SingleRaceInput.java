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
import java.util.List;

public abstract class SingleRaceInput extends RaceInput {

    public SingleRaceInput(Race race) {

        super(race);

        readProperties();
        constructFilePaths();
    }

    protected void readProperties() {

        entries_filename = race.getProperties().getProperty("ENTRIES_FILENAME");
        raw_results_filename = race.getProperties().getProperty("RAW_RESULTS_FILENAME");
    }

    protected void constructFilePaths() {

        input_directory_path = race.getWorkingDirectoryPath().resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);
    }

    protected List<RaceEntry> loadEntries() throws IOException {

        final List<String> lines = Files.readAllLines(entries_path);
        final List<RaceEntry> entries = new ArrayList<>();

        for (final String line : lines) {

            final RaceEntry entry = makeRaceEntry(line.split("\t"));

            checkDuplicateBibNumber(entries, entry);
            checkDuplicateEntry(entries, entry);

            entries.add(entry);
        }

        return entries;
    }

    protected List<RawResult> loadRawResults(final Path results_path) throws IOException {

        final List<RawResult> raw_results = new ArrayList<>();

        for (final String line : Files.readAllLines(results_path))
            loadRawResult(raw_results, line);

        return raw_results;
    }

    private void loadRawResult(final List<RawResult> raw_results, String line) {

        final int comment_start_index = line.indexOf("#");
        if (comment_start_index > -1) line = line.substring(0, comment_start_index);

        if (!line.isBlank()) {

            try {
                final RawResult result = new RawResult(line);
                checkOrdering(raw_results, result);

                raw_results.add(result);
            }
            catch (NumberFormatException ignored) {
            }
        }
    }

    private void checkOrdering(final List<RawResult> raw_results, final RawResult result) {

        final RawResult previous_result = !raw_results.isEmpty() ? raw_results.get(raw_results.size() - 1) : null;

        if (resultsAreOutOfOrder(result, previous_result))
            throw new RuntimeException("result " + (raw_results.size() + 1) + " out of order");
    }

    private boolean resultsAreOutOfOrder(final RawResult result, final RawResult previous_result) {

        return result.getRecordedFinishTime() != null &&
                previous_result != null && previous_result.getRecordedFinishTime() != null &&
                previous_result.getRecordedFinishTime().compareTo(result.getRecordedFinishTime()) > 0;
    }

    private void checkDuplicateBibNumber(final List<RaceEntry> entries, final RaceEntry new_entry) {

        for (final RaceEntry entry : entries)
            if (entry != null && entry.bib_number == new_entry.bib_number)
                throw new RuntimeException("duplicate bib number: " + new_entry.bib_number);
    }

    protected abstract List<RawResult> loadRawResults() throws IOException;
    protected abstract void checkDuplicateEntry(final List<RaceEntry> entries, final RaceEntry new_entry);
    protected abstract RaceEntry makeRaceEntry(final String[] elements);
}
