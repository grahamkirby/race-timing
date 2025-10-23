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
import org.grahamkirby.race_timing.categories.CategoryDetails;
import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.CommonDataProcessor.*;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.readAllLines;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateBibNumbersUnique;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateRawResults;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateRawResultsOrdering;
import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.RaceEntry.CATEGORY_INDEX;

public class IndividualRace implements SingleRaceInternal {

    // Components:
    //
    // Config
    // Results calculator
    // Categories processor
    // Output handling

    private static final int NUMBER_OF_ENTRY_COLUMNS = 4;

    Map<EntryCategory, Duration> category_start_offsets;
    Map<Integer, Duration> individual_start_offsets;
    Map<Integer, Duration> time_trial_start_offsets;
    Map<Integer, Duration> separately_recorded_finish_times;

    private final Config config;
    private CategoriesProcessor categories_processor;
    private CategoryDetails category_details;
    private RaceResultsCalculator results_calculator;
    private RaceOutput results_output;
    private Normalisation normalisation;
    private final Notes notes;
    private List<RawResult> raw_results;
    private List<RaceEntry> entries;

    public IndividualRace(final Config config) {

        this.config = config;
        notes = new Notes();
    }

    public Config getConfig() {
        return config;
    }

    public void setCategoriesProcessor(final CategoriesProcessor categories_processor) {

        this.categories_processor = categories_processor;
    }

    public void setResultsCalculator(final RaceResultsCalculator results_calculator) {

        this.results_calculator = results_calculator;
    }

    public void setResultsOutput(final RaceOutput results_output) {

        this.results_output = results_output;
    }

    private void loadRaceData() {

        final Path entries_path = config.getPathConfig(KEY_ENTRIES_PATH);
        final Path raw_results_path = config.getPathConfig(KEY_RAW_RESULTS_PATH);

        try {
            validateDataFiles(entries_path, raw_results_path);

            entries = loadEntries(entries_path);
            raw_results = loadRawResults(raw_results_path);

            validateData(entries, raw_results, entries_path, raw_results_path);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateEntryCategory(final String line) {

        final List<String> elements = Arrays.stream(line.split("\t")).toList();
        final Normalisation normalisation = getNormalisation();
        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            getCategoryDetails().lookupEntryCategory(category_name);

        } catch (final RuntimeException e) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    private void validateDataFiles(final Path entries_path, final Path raw_results_path) throws IOException {

        validateEntriesNumberOfElements(entries_path, NUMBER_OF_ENTRY_COLUMNS, config.getStringConfig(KEY_ENTRY_COLUMN_MAP));
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

    private List<RaceEntry> loadEntries(final Path entries_path) throws IOException {

        return readAllLines(entries_path).stream().
            map(Normalisation::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> new RaceEntry(Arrays.stream(line.split("\t")).toList(), this)).
            toList();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public List<RaceEntry> getEntries() {
        return entries;
    }

    @Override
    public void processResults() {

        category_details = categories_processor.getCategoryDetails();
        loadRaceData();
        completeConfiguration2();
        results_calculator.calculateResults();
    }

    @Override
    public void outputResults() throws IOException {
        results_output.outputResults();
    }

    public void completeConfiguration2() {

        category_start_offsets = readCategoryStartOffsets();
        individual_start_offsets = loadIndividualStartOffsets();
        time_trial_start_offsets = loadTimeTrialStarts();
        separately_recorded_finish_times = readSeparatelyRecordedResults();
    }

    @Override
    public RaceResultsCalculator getResultsCalculator() {
        return results_calculator;
    }

    @Override
    public CategoryDetails getCategoryDetails() {
        return category_details;
    }

    @Override
    public synchronized Normalisation getNormalisation() {

        if (normalisation == null)
            normalisation = new Normalisation(this);

        return normalisation;
    }

    @Override
    public Notes getNotes() {
        return notes;
    }

//    @Override
//    public void appendToNotes(String s) {
//        results_calculator.getNotes().append(s);
//    }
//
//    @Override
//    public String getNotes() {
//        return results_calculator.getNotes().toString();
//    }

    private Map<EntryCategory, Duration> readCategoryStartOffsets() {

        final Map<EntryCategory, Duration> category_offsets = new HashMap<>();

        final Consumer<Object> process_category_start_offsets = value -> {

            // e.g. FU9/00:01:00,MU9/00:01:00,FU11/00:01:00,MU11/00:01:00
            final String[] offset_strings = ((String) value).split(",", -1);

            for (final String offset_string : offset_strings) {

                final String[] split = offset_string.split("/");
                category_offsets.put(category_details.lookupEntryCategory(split[0]), parseTime(split[1]));
            }
        };

        config.processConfigIfPresent(KEY_CATEGORY_START_OFFSETS, process_category_start_offsets);

        return category_offsets;
    }

    private Map<Integer, Duration> readSeparatelyRecordedResults() {

        final Map<Integer, Duration> results = new HashMap<>();

        final Consumer<Object> process_separately_recorded_results = value -> {

            final String[] self_timed_strings = ((String) value).split(",", -1);

            // SEPARATELY_RECORDED_RESULTS = 126/8:09

            for (final String s : self_timed_strings) {

                final String[] split = s.split("/", -1);
                final int bib_number = Integer.parseInt(split[0]);
                final Duration finish_time = parseTime(split[1]);

                results.put(bib_number, finish_time);
            }
        };

        config.processConfigIfPresent(KEY_SEPARATELY_RECORDED_RESULTS, process_separately_recorded_results);

        return results;
    }

    private Map<Integer, Duration> loadTimeTrialStarts() {

        final Map<Integer, Duration> starts = new HashMap<>();

        // The first option applies when time-trial runners are assigned to waves in order of bib number,
        // with incomplete waves if there are any gaps in bib numbers.
        // The second option applies when start order is manually determined (e.g. to start current leaders first or last).

        final Consumer<Object> process_runners_per_wave = value -> {

            final int time_trial_runners_per_wave = (int) value;
            final Duration time_trial_inter_wave_interval = (Duration) getConfig().get(KEY_TIME_TRIAL_INTER_WAVE_INTERVAL);

            for (final RawResult raw_result : raw_results) {

                final int wave_number = (raw_result.getBibNumber() - 1) / time_trial_runners_per_wave;
                final Duration start_offset = time_trial_inter_wave_interval.multipliedBy(wave_number);

                starts.put(raw_result.getBibNumber(), start_offset);
            }
        };

        final Consumer<Object> process_explicit_starts = value -> {

            for (final String part : ((String) value).split(",", -1)) {

                final String[] split = part.split("/");
                starts.put(Integer.parseInt(split[0]), parseTime(split[1]));
            }
        };

        config.processConfigIfPresent(KEY_TIME_TRIAL_RUNNERS_PER_WAVE, process_runners_per_wave);
        config.processConfigIfPresent(KEY_TIME_TRIAL_STARTS, process_explicit_starts);

        return starts;
    }

    private Map<Integer, Duration> loadIndividualStartOffsets() {

        final Map<Integer, Duration> starts = new HashMap<>();

        // bib number / start time difference
        // Example: INDIVIDUAL_EARLY_STARTS = 2/0:10:00,26/0:20:00

        final Consumer<Object> process_early_starts = value -> {

            for (final String individual_early_start : ((String) value).split(",")) {

                final String[] split = individual_early_start.split("/");

                final int bib_number = Integer.parseInt(split[0]);
                final Duration offset = parseTime(split[1]);

                // Offset will be subtracted from recorded time,
                // so negate since a positive time corresponds to an early start.

                starts.put(bib_number, offset.negated());
            }
        };

        config.processConfigIfPresent(KEY_INDIVIDUAL_EARLY_STARTS, process_early_starts);

        return starts;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<String> getTeamPrizes() {

        int best_male_team_total = Integer.MAX_VALUE;
        int best_female_team_total = Integer.MAX_VALUE;
        String best_male_team = "";
        String best_female_team = "";

        for (final String club : getClubs()) {

            final int male_team_total = getTeamTotal(club, "Men");
            final int female_team_total = getTeamTotal(club, "Women");

            if (male_team_total < best_male_team_total) {
                best_male_team = club;
                best_male_team_total = male_team_total;
            }

            if (female_team_total < best_female_team_total) {
                best_female_team = club;
                best_female_team_total = female_team_total;
            }
        }

        final List<String> prizes = new ArrayList<>();

        if (best_male_team_total < Integer.MAX_VALUE)
            prizes.add("First male team: " + best_male_team + " (" + best_male_team_total + ")");

        if (best_female_team_total < Integer.MAX_VALUE)
            prizes.add("First female team: " + best_female_team + " (" + best_female_team_total + ")");

        return prizes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private int getTeamTotal(final String club, final String gender) {

        final int number_to_count_for_team_prize = (int) getConfig().get(KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE);

        int result_position = 0;
        int team_count = 0;
        int total = 0;

            for (final RaceResult result : getResultsCalculator().getOverallResults()) {

            result_position++;

            final Runner runner = (Runner) result.getParticipant();

            if (team_count < number_to_count_for_team_prize && runner.getClub().equals(club) && runner.getCategory().getGender().equals(gender)) {
                team_count++;
                total += result_position;
            }
        }

        return team_count >= number_to_count_for_team_prize ? total : Integer.MAX_VALUE;
    }

    private Set<String> getClubs() {

        return getResultsCalculator().getOverallResults().stream().
            map(result -> ((Runner) result.getParticipant()).getClub()).
            collect(Collectors.toSet());
    }
}
