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

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.RaceConfigValidator.*;
import static org.grahamkirby.race_timing.common.RaceEntry.CATEGORY_INDEX;

public class IndividualRace implements SingleRaceInternal {

    private static final int NUMBER_OF_ENTRY_COLUMNS = 4;

    private Map<Integer, Duration> separately_recorded_finish_times;
    private List<RawResult> raw_results;
    private List<RaceEntry> entries;

    private final Config config;
    private final CategoriesProcessor categories_processor;
    private final Normalisation normalisation;
    private final Notes notes;

    private RaceResultsCalculator results_calculator;
    private RaceOutput results_output;

    public IndividualRace(final Config config) throws IOException {

        this.config = config;
        notes = new Notes();
        categories_processor = new CategoriesProcessor(config);
        normalisation = new Normalisation(config);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CategoriesProcessor getCategoriesProcessor() {
        return categories_processor;
    }

    @Override
    public void setResultsCalculator(final RaceResultsCalculator results_calculator) {
        this.results_calculator = results_calculator;
    }

    @Override
    public void setResultsOutput(final RaceOutput results_output) {
        this.results_output = results_output;
    }

    @Override
    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public List<RaceEntry> getEntries() {
        return entries;
    }

    @Override
    public RaceResults processResults() throws IOException {

        loadRaceData();
        loadSeparatelyRecordedResults();
        return results_calculator.calculateResults();
    }

    @Override
    public void outputResults(final RaceResults results) throws IOException {

        results_output.outputResults(results);
        config.outputUnusedProperties();
    }

    @Override
    public RaceResultsCalculator getResultsCalculator() {
        return results_calculator;
    }

    @Override
    public Normalisation getNormalisation() {

        return normalisation;
    }

    @Override
    public Notes getNotes() {
        return notes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Map<Integer, Duration> getSeparatelyRecordedFinishTimes() {
        return separately_recorded_finish_times;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadRaceData() {

        final Path entries_path = config.getPath(KEY_ENTRIES_PATH);
        final Path raw_results_path = config.getPath(KEY_RAW_RESULTS_PATH);

        try {
            validateDataFiles(entries_path, raw_results_path);

            entries = loadEntries(entries_path);
            raw_results = loadRawResults(raw_results_path);

            validateData(entries, raw_results, entries_path, raw_results_path);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<RaceEntry> loadEntries(final Path entries_path) throws IOException {

        return readAllLines(entries_path).stream().
            map(Normalisation::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> new RaceEntry(Arrays.stream(line.split("\t")).toList(), this)).
            toList();
    }

    private List<RawResult> loadRawResults(final Path raw_results_path) throws IOException {

        return readAllLines(raw_results_path).stream().
            map(Normalisation::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(RawResult::new).
            toList();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void validateEntryCategory(final String line) {

        final List<String> elements = Arrays.stream(line.split("\t")).toList();
        final Normalisation normalisation = getNormalisation();
        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            getCategoriesProcessor().getEntryCategory(category_name);

        } catch (final RuntimeException e) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    private void validateDataFiles(final Path entries_path, final Path raw_results_path) throws IOException {

        validateEntriesNumberOfElements(entries_path, NUMBER_OF_ENTRY_COLUMNS, config.getString(KEY_ENTRY_COLUMN_MAP));
        validateEntryCategories(entries_path, this::validateEntryCategory);
        validateBibNumbers(entries_path);
        validateRawResults(raw_results_path);
        validateBibNumbers(raw_results_path);
        validateRawResultsOrdering(raw_results_path);
    }

    private void validateData(final List<RaceEntry> entries, final List<RawResult> raw_results, final Path entries_path, final Path raw_results_path) throws IOException {

        validateEntriesUnique(entries, entries_path);
        validateRecordedBibNumbersAreRegistered(entries, raw_results_path);
    }

    private void validateEntriesUnique(final List<RaceEntry> entries, final Path entries_path) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1.getParticipant() != entry2.getParticipant() && entry1.getParticipant().equals(entry2.getParticipant()))
                    throw new RuntimeException("duplicate entry '" + entry1 + "' in file '" + entries_path.getFileName() + "'");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadSeparatelyRecordedResults() {

        separately_recorded_finish_times = new HashMap<>();

        final Consumer<Object> process_separately_recorded_results = value -> {

            final String[] self_timed_strings = ((String) value).split(",", -1);

            // Example: SEPARATELY_RECORDED_RESULTS = 126/8:09

            for (final String s : self_timed_strings) {

                final String[] split = s.split("/", -1);
                final int bib_number = Integer.parseInt(split[0]);
                final Duration finish_time = parseTime(split[1]);

                separately_recorded_finish_times.put(bib_number, finish_time);
            }
        };

        config.processConfigIfPresent(KEY_SEPARATELY_RECORDED_RESULTS, process_separately_recorded_results);
    }
}
