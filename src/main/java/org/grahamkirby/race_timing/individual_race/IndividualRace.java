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

import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.CommonDataProcessor.*;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.readAllLines;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateBibNumbersUnique;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateRawResults;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateRawResultsOrdering;
import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.RaceEntry.CATEGORY_INDEX;

public class IndividualRace implements SpecificRace, Race2, RaceData, RaceDataProcessor {

    // Components:
    //
    // Config
    // Raw data manager
    // Results calculator
    // Categories processor
    // Output handling

    private Race race;
    Map<EntryCategory, Duration> category_start_offsets;
    Map<Integer, Duration> individual_start_offsets;
    Map<Integer, Duration> time_trial_start_offsets;
    Map<Integer, Duration> separately_recorded_finish_times;

    int time_trial_runners_per_wave;
    Duration time_trial_inter_wave_interval;

    private final List<ConfigProcessor> config_processors = new ArrayList<>();
    private Config config;
    public Path config_file_path;

    private static final int NUMBER_OF_ENTRY_COLUMNS = 4;

    public void addConfigProcessor(final ConfigProcessor processor) {

        config_processors.add(processor);
    }

    public void loadConfig() throws IOException {

        config = new Config(config_file_path);

        for (final ConfigProcessor processor : config_processors) {

            processor.processConfig(this);
        }
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public RaceData getRaceData() {

        final Path entries_path = race.getPathConfig(KEY_ENTRIES_PATH);
        final Path raw_results_path = race.getPathConfig(KEY_RAW_RESULTS_PATH);

        try {
            validateDataFiles(entries_path, raw_results_path);

//            final List<RaceEntry> entries = loadEntries(entries_path);
//            final List<RawResult> raw_results = loadRawResults(raw_results_path);
            entries = loadEntries(entries_path);
            raw_results = loadRawResults(raw_results_path);

            validateData(entries, raw_results, entries_path, raw_results_path);

            return this;
//            return new IndividualRace(raw_results, entries);
//            return new IndividualRaceDataImpl(raw_results, entries);

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





    //////////////////////////////////////////////////////////////////////////////////////////////////

    private  List<RawResult> raw_results;
    private  List<RaceEntry> entries;

    public IndividualRace() {


    }

    public IndividualRace(final Path config_file_path) {


            this.config_file_path = config_file_path;

    }

    public IndividualRace(List<RawResult> raw_results, List<RaceEntry> entries) {

        this.raw_results = raw_results;
        this.entries = entries;
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
    public void processResults() {

        race.processResults();
    }

    @Override
    public void outputResults() throws IOException {

        race.outputResults();
    }


    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public void completeConfiguration() {

        category_start_offsets = readCategoryStartOffsets();
        individual_start_offsets = loadIndividualStartOffsets();
        time_trial_start_offsets = loadTimeTrialStarts();
        separately_recorded_finish_times = readSeparatelyRecordedResults();
    }

//    @Override
//    public Config getConfig() {
//        return race.getConfig();
//    }

    @Override
    public RaceResultsCalculator getResultsCalculator() {
        return race.getResultsCalculator();
    }

    /**
     * Resolves the given path relative to either the race configuration file,
     * if it's specified as a relative path, or to the project root. Examples:
     *
     * Relative to race configuration:
     * entries.txt -> /Users/gnck/Desktop/myrace/input/entries.txt
     *
     * Relative to project root:
     * /src/main/resources/configuration/categories_entry_individual_senior.csv ->
     *    src/main/resources/configuration/categories_entry_individual_senior.csv
     */
    @SuppressWarnings("JavadocBlankLines")
    public Path interpretPath(final Path path) {

        // Absolute paths originate from config file where path starting with "/" denotes
        // a path relative to the project root.
        // Can't test with isAbsolute() since that will return false on Windows.
        if (path.startsWith("/")) return makeRelativeToProjectRoot(path);

        return getPathRelativeToRaceConfigFile(path);
    }

    private static Path makeRelativeToProjectRoot(final Path path) {

        // Path is specified as absolute path, should be reinterpreted relative to project root.
        return path.subpath(0, path.getNameCount());
    }

    private Path getPathRelativeToRaceConfigFile(final Path path) {

        return config_file_path.resolveSibling(path);
    }

    private Map<EntryCategory, Duration> readCategoryStartOffsets() {

        final Map<EntryCategory, Duration> category_offsets = new HashMap<>();

        if (race.getConfig().containsKey(KEY_CATEGORY_START_OFFSETS)) {

            // e.g. FU9/00:01:00,MU9/00:01:00,FU11/00:01:00,MU11/00:01:00
            final String[] offset_strings = ((String) (race.getConfig().get(KEY_CATEGORY_START_OFFSETS))).split(",", -1);

            for (final String offset_string : offset_strings) {

                final String[] split = offset_string.split("/");
                category_offsets.put(race.getCategoryDetails().lookupEntryCategory(split[0]), Normalisation.parseTime(split[1]));
            }
        }

        return category_offsets;
    }

    private Map<Integer, Duration> readSeparatelyRecordedResults() {

        final Map<Integer, Duration> results = new HashMap<>();

        if (race.getConfig().containsKey(KEY_SEPARATELY_RECORDED_RESULTS)) {

            final String[] self_timed_strings = ((String) (race.getConfig().get(KEY_SEPARATELY_RECORDED_RESULTS))).split(",", -1);

            // SEPARATELY_RECORDED_RESULTS = 126/8:09

            for (final String s : self_timed_strings) {

                final String[] split = s.split("/", -1);
                final int bib_number = Integer.parseInt(split[0]);
                final Duration finish_time = Normalisation.parseTime(split[1]);

                results.put(bib_number, finish_time);
            }
        }

        return results;
    }

    private Map<Integer, Duration> loadTimeTrialStarts() {

        final Map<Integer, Duration> starts = new HashMap<>();

        // The first option applies when time-trial runners are assigned to waves in order of bib number,
        // with incomplete waves if there are any gaps in bib numbers.
        // The second option applies when start order is manually determined (e.g. to start current leaders first or last).

        if (race.getConfig().containsKey(KEY_TIME_TRIAL_RUNNERS_PER_WAVE)) {

            time_trial_runners_per_wave = (int) race.getConfig().get(KEY_TIME_TRIAL_RUNNERS_PER_WAVE);
            time_trial_inter_wave_interval = (Duration) race.getConfig().get(KEY_TIME_TRIAL_INTER_WAVE_INTERVAL);

            for (final RawResult raw_result : race.getRaceData().getRawResults()) {

                final int wave_number = runnerIndexInBibOrder(raw_result.getBibNumber()) / time_trial_runners_per_wave;
                final Duration start_offset = time_trial_inter_wave_interval.multipliedBy(wave_number);

                starts.put(raw_result.getBibNumber(), start_offset);
            }
        }

        if (race.getConfig().containsKey(KEY_TIME_TRIAL_STARTS)) {

            for (final String part : ((String) race.getConfig().get(KEY_TIME_TRIAL_STARTS)).split(",", -1)) {

                final String[] split = part.split("/");
                starts.put(Integer.parseInt(split[0]), Normalisation.parseTime(split[1]));
            }
        }

        return starts;
    }

    private Map<Integer, Duration> loadIndividualStartOffsets() {

        final Map<Integer, Duration> starts = new HashMap<>();

        final String individual_early_starts_string = (String) race.getConfig().get(KEY_INDIVIDUAL_EARLY_STARTS);

        // bib number / start time difference
        // Example: INDIVIDUAL_EARLY_STARTS = 2/0:10:00,26/0:20:00

        if (individual_early_starts_string != null)
            for (final String individual_early_start : individual_early_starts_string.split(",")) {

                final String[] split = individual_early_start.split("/");

                final int bib_number = Integer.parseInt(split[0]);
                final Duration offset = Normalisation.parseTime(split[1]);

                // Offset will be subtracted from recorded time,
                // so negate since a positive time corresponds to an early start.

                starts.put(bib_number, offset.negated());
            }

        return starts;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static int runnerIndexInBibOrder(final int bib_number) {
        return bib_number - 1;
    }

    public List<String> getTeamPrizes() {

        final List<RaceResult> overall_results = race.getResultsCalculator().getOverallResults();
        final Set<String> clubs = getClubs(overall_results);

        int best_male_team_total = Integer.MAX_VALUE;
        String best_male_team = "";
        int best_female_team_total = Integer.MAX_VALUE;
        String best_female_team = "";

        for (final String club : clubs) {

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

        final int number_to_count_for_team_prize = (Integer) race.getConfig().get(KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE);

        int result_position = 0;
        int team_count = 0;
        int total = 0;

        for (final RaceResult result : race.getResultsCalculator().getOverallResults()) {

            result_position++;

            final Runner runner = (Runner) result.getParticipant();

            if (team_count < number_to_count_for_team_prize && runner.getClub().equals(club) && runner.getCategory().getGender().equals(gender)) {
                team_count++;
                total += result_position;
            }
        }

        return team_count >= number_to_count_for_team_prize ? total : Integer.MAX_VALUE;
    }

    private Set<String> getClubs(final List<RaceResult> results) {

        return results.stream().
            map(result -> ((Runner) result.getParticipant()).getClub()).
            collect(Collectors.toSet());
    }
}
