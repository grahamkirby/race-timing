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

import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceEntry;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Race.*;

public abstract class SingleRaceInput extends RaceInput {

    private final Function<String, RaceEntry> race_entry_mapper = line -> makeRaceEntry(Arrays.stream(line.split("\t")).toList());
    private final Function<String, RaceResult> race_result_mapper = line -> makeRaceResult(new ArrayList<>(Arrays.stream(line.split("\t")).toList()));

    private int next_fake_bib_number = 1;

    protected SingleRaceInput(final Race race) {

        super(race);
        readProperties();
    }

    protected void readProperties() {

        entries_path = race.getProperty(KEY_ENTRIES_PATH);
        raw_results_path = race.getProperty(KEY_RAW_RESULTS_PATH);
        results_path = race.getProperty(KEY_RESULTS_PATH);
        categories_entry_path = race.getProperty(KEY_CATEGORIES_ENTRY_PATH);
        categories_prize_path = race.getProperty(KEY_CATEGORIES_PRIZE_PATH);
    }

    List<RaceEntry> loadEntries() throws IOException {

        if (entries_path == null) return List.of();

        final List<RaceEntry> entries = Files.readAllLines(race.getPath(entries_path)).stream().
            filter(line -> !line.isEmpty()).
            map(race_entry_mapper).
            toList();

        assertNoDuplicateBibNumbers(entries);
        assertNoDuplicateEntries(entries);

        return entries;
    }

    List<RaceResult> loadOverallResults() throws IOException {

        if (results_path == null) return new ArrayList<>();

        return Files.readAllLines(race.getPath(results_path)).stream().
            filter(line -> !line.isEmpty()).
            map(race_result_mapper).
            toList();
    }

    private RaceResult makeRaceResult(final List<String> elements) {

        elements.addFirst(String.valueOf(next_fake_bib_number++));

        final IndividualRaceEntry entry = new IndividualRaceEntry(elements, race);
        final IndividualRaceResult result = new IndividualRaceResult((IndividualRace) race, entry);

        result.finish_time = Normalisation.parseTime(elements.getLast());
        result.completion_status = CompletionStatus.COMPLETED;

        return result;
    }

    protected List<RawResult> loadRawResults(final Path results_path) throws IOException {

        if (raw_results_path == null) return List.of();

        final List<RawResult> raw_results = new ArrayList<>();

        for (final String line : Files.readAllLines(results_path))
            addResult(stripComment(line), raw_results);

        assertCorrectlyOrdered(raw_results);

        return raw_results;
    }

    private void addResult(final String line, final Collection<? super RawResult> raw_results) {

        if (!line.isBlank())
            try {
                raw_results.add(loadRawResult(line));

            } catch (final NumberFormatException _) {
            }
    }

    protected RawResult loadRawResult(final String line) {

        return new RawResult(line);
    }

    private static String stripComment(final String line) {

        final int comment_start_index = line.indexOf(COMMENT_SYMBOL);
        return (comment_start_index > -1) ? line.substring(0, comment_start_index) : line;
    }

    private static void assertCorrectlyOrdered(final List<? extends RawResult> raw_results) {

        for (int i = 0; i < raw_results.size() - 1; i++) {

            final RawResult result1 = raw_results.get(i);
            final RawResult result2 = raw_results.get(i + 1);

            if (areResultsOutOfOrder(result1, result2))
                throw new RuntimeException(STR."result \{i + 2} out of order");
        }
    }

    private static boolean areResultsOutOfOrder(final RawResult result1, final RawResult result2) {

        return result2.getRecordedFinishTime() != null &&
            result1.getRecordedFinishTime() != null &&
            result1.getRecordedFinishTime().compareTo(result2.getRecordedFinishTime()) > 0;
    }

    private static void assertNoDuplicateBibNumbers(final Iterable<? extends RaceEntry> entries) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1 != entry2 && entry1.bib_number == entry2.bib_number)
                    throw new RuntimeException(STR."duplicate bib number: \{entry1.bib_number}");
    }

    private static void assertNoDuplicateEntries(final Iterable<? extends RaceEntry> entries) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1 != entry2 && entry1.equals(entry2))
                    throw new RuntimeException(STR."duplicate entry: \{entry1}");
    }

    protected abstract List<RawResult> loadRawResults() throws IOException;
    protected abstract RaceEntry makeRaceEntry(final List<String> elements);
}
