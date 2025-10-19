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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.CommonDataProcessor.*;
import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.RaceEntry.CATEGORY_INDEX;

public class IndividualRaceDataProcessorImpl implements RaceDataProcessor {

    private static final int NUMBER_OF_ENTRY_COLUMNS = 4;
    private Race race;

    @Override
    public void setRace(final Race race) {
        this.race = race;
    }

    @Override
    public RaceData getRaceData() {

        final Path entries_path = race.getPathConfig(KEY_ENTRIES_PATH);
        final Path raw_results_path = race.getPathConfig(KEY_RAW_RESULTS_PATH);

        try {
            validateDataFiles(entries_path, raw_results_path);

            final List<RaceEntry> entries = loadEntries(entries_path);
            final List<RawResult> raw_results = loadRawResults(raw_results_path);

            validateData(entries, raw_results, entries_path, raw_results_path);

            return new IndividualRaceDataImpl(raw_results, entries);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateEntryCategory(final String line) {

        final List<String> elements = Arrays.stream(line.split("\t")).toList();
        final Normalisation normalisation = race.getNormalisation();
        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            race.getCategoryDetails().lookupEntryCategory(category_name);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    private void validateDataFiles(final Path entries_path, final Path raw_results_path) throws IOException {

        validateEntriesNumberOfElements(entries_path, NUMBER_OF_ENTRY_COLUMNS, (String) race.getConfig().get(KEY_ENTRY_COLUMN_MAP));
        validateEntryCategories(entries_path, this::validateEntryCategory);
        validateBibNumbersUnique(entries_path);
        validateRawResults(raw_results_path);
        validateBibNumbersUnique(raw_results_path);
        validateRawResultsOrdering(raw_results_path);
    }

    private void validateData(final List<RaceEntry> entries, final List<RawResult> raw_results, final Path entries_path, final Path raw_results_path) {

        validateEntriesUnique(entries, entries_path);
        validateRecordedBibNumbersAreRegistered(entries, raw_results, raw_results_path);
    }

    private List<RawResult> loadRawResults(final Path raw_results_path) throws IOException {

        return readAllLines(raw_results_path).stream().
            map(Normalisation::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(RawResult::new).
            toList();
    }

    private void validateRecordedBibNumbersAreRegistered(final List<RaceEntry> entries, final List<RawResult> raw_results, final Path raw_results_path) {

        final Set<Integer> entry_bib_numbers = entries.stream().
            map(entry -> entry.bib_number).
            collect(Collectors.toSet());

        final AtomicInteger line = new AtomicInteger(0);

        raw_results.forEach(raw_result -> {
            line.incrementAndGet();
            final int result_bib_number = raw_result.getBibNumber();

            if (result_bib_number != UNKNOWN_BIB_NUMBER && !entry_bib_numbers.contains(result_bib_number))
                throw new RuntimeException("unregistered bib number '" + result_bib_number + "' at line " + line.get() + " in file '" + raw_results_path.getFileName() + "'");
        });
    }

    private void validateEntriesUnique(final List<RaceEntry> entries, final Path entries_path) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1.participant != entry2.participant && entry1.participant.equals(entry2.participant))
                    throw new RuntimeException("duplicate entry '" + entry1 + "' in file '" + entries_path.getFileName() + "'");
    }

    List<RaceEntry> loadEntries(final Path entries_path) throws IOException {

        return readAllLines(entries_path).stream().
            map(Normalisation::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> new RaceEntry(Arrays.stream(line.split("\t")).toList(), race)).
            toList();
    }
}
