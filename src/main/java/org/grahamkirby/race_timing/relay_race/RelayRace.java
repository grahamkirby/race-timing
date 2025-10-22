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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.categories.CategoryDetails;
import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.CommonDataProcessor.*;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.readAllLines;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateRawResults;
import static org.grahamkirby.race_timing.common.CommonDataProcessor.validateRawResultsOrdering;
import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.*;

public class RelayRace implements Race2 {

    private static final int BIB_NUMBER_INDEX = 0;
    private static final int TEAM_NAME_INDEX = 1;
    private static final int CATEGORY_INDEX = 2;
    private static final int FIRST_RUNNER_NAME_INDEX = 3;

    final Map<RawResult, Integer> explicitly_recorded_leg_numbers = new HashMap<>();
    int number_of_electronically_recorded_raw_results;

    private List<RawResult> raw_results;
    private List<RaceEntry> entries;
    private CategoryDetails category_details;
    private RaceResultsCalculator results_calculator;
    private final Config config;
    private CategoriesProcessor categories_processor;
    private ResultsOutput results_output;
    private Normalisation normalisation;

    public RelayRace(final Config config) throws IOException {

        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public Object getSpecific() {
        return this;
    }

    public CategoryDetails getCategoryDetails() {
        return category_details;
    }

    private void loadRaceData() {

        final Path entries_path = config.getPathConfig(KEY_ENTRIES_PATH);
        final Path electronic_results_path = config.getPathConfig(KEY_RAW_RESULTS_PATH);
        final Path paper_results_path = config.getPathConfig(KEY_PAPER_RESULTS_PATH);

        try {
            validateDataFiles(entries_path, electronic_results_path, paper_results_path);

            entries = loadEntries(entries_path);

            final List<RawResult> electronically_recorded_raw_results = loadRawResults(electronic_results_path);
            final List<RawResult> paper_recorded_raw_results = loadRawResults(paper_results_path);

            number_of_electronically_recorded_raw_results = electronically_recorded_raw_results.size();
            raw_results = append(electronically_recorded_raw_results, paper_recorded_raw_results);

            config.processConfigIfPresent(KEY_ANNOTATIONS_PATH, this::processAnnotations);
            validateData(entries, entries_path, raw_results, electronic_results_path, paper_results_path);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateEntryCategory(final String line) {

        final List<String> elements = Arrays.stream(line.split("\t")).toList();
        final Normalisation normalisation = getNormalisation();
        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            category_details.lookupEntryCategory(category_name);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    private void validateDataFiles(final Path entries_path, final Path electronic_results_path, final Path paper_results_path) throws IOException {

        validateEntriesNumberOfElements(entries_path, getNumberOfLegs() + 3, config.getStringConfig(KEY_ENTRY_COLUMN_MAP));
        validateEntryCategories(entries_path, this::validateEntryCategory);
        validateBibNumbersUnique(entries_path);

        validateRawResults(electronic_results_path);
        validateRawResults(paper_results_path);

        validateNumberOfLegResults(electronic_results_path, paper_results_path);
        validateRawResultsOrdering(electronic_results_path);
        validateRawResultsOrdering(paper_results_path);
    }

    private void validateData(final List<RaceEntry> entries, final Path entries_path, final List<RawResult> combined_raw_results, final Path electronic_results_path, Path paper_results_path) {

        validateEntriesUnique(entries, entries_path);
        validateRecordedBibNumbersAreRegistered(entries, combined_raw_results, electronic_results_path, paper_results_path);
    }

    private List<RawResult> append(final List<RawResult> list1, final List<RawResult> list2) {

        final List<RawResult> result = new ArrayList<>(list1);
        result.addAll(list2);
        return result;
    }

    protected List<RawResult> loadRawResults(final Path results_path) throws IOException {

        return readAllLines(results_path).stream().
            map(Normalisation::stripComment).
            filter(Predicate.not(String::isBlank)).
            map(this::makeRawResult).
            toList();
    }

    private RawResult makeRawResult(final String line) {

        final RawResult result = new RawResult(line);

        final int leg_number = getExplicitLegNumber(line);
        if (leg_number > 0) explicitly_recorded_leg_numbers.put(result, leg_number);

        return result;
    }

    private int getExplicitLegNumber(final String line) {

        final String[] elements = line.split("\t");
        return elements.length > 2 ? Integer.parseInt(elements[2]) : 0;
    }

    private void validateRecordedBibNumbersAreRegistered(final List<RaceEntry> entries, final List<RawResult> raw_results, final Path electronic_results_path, final Path paper_results_path) {

        final Set<Integer> entry_bib_numbers = entries.stream().
            map(entry -> entry.bib_number).
            collect(Collectors.toSet());

        int line = 0;
        for (final RawResult raw_result : raw_results) {

            line++;
            final int result_bib_number = raw_result.getBibNumber();
            if (result_bib_number != UNKNOWN_BIB_NUMBER && !entry_bib_numbers.contains(result_bib_number)) {
                String message = "unregistered bib number '" + result_bib_number + "' at line " + line + " in file '" + electronic_results_path.getFileName() + "'";
                if (paper_results_path != null) message += " or '" + paper_results_path.getFileName() + "'";
                throw new RuntimeException(message);
            }
        }
    }

    private void validateEntriesUnique(final List<RaceEntry> entries, final Path entries_path) {

        for (final RaceEntry entry1 : entries)
            for (final RaceEntry entry2 : entries)
                if (entry1.participant != entry2.participant && entry1.participant.equals(entry2.participant))
                    throw new RuntimeException("duplicate entry '" + entry1 + "' in file '" + entries_path.getFileName() + "'");
    }

    private void validateNumberOfLegResults(final Path raw_results_path, final Path paper_results_path) {

        try {
            final Map<String, Integer> bib_counts = new HashMap<>();

            countLegResults(bib_counts, raw_results_path);
            countLegResults(bib_counts, paper_results_path);

            for (final Map.Entry<String, Integer> entry : bib_counts.entrySet())
                if (entry.getValue() > getNumberOfLegs()) {
                    String message = "surplus result for team '" + entry.getKey() + "' in file '" + raw_results_path.getFileName() + "'";
                    if (paper_results_path != null)
                        message += " or '" + paper_results_path.getFileName() + "'";
                    throw new RuntimeException(message);
                }
        } catch (final IOException e) {
            throw new RuntimeException("unexpected IO exception", e);
        }
    }

    private void countLegResults(final Map<String, Integer> bib_counts, final Path results_path) throws IOException {

        if (results_path != null)
            for (final String line : Files.readAllLines(results_path))
                // TODO rationalise with other comment handling. Use stripComment.
                if (!line.startsWith(COMMENT_SYMBOL) && !line.isBlank()) {

                    final String bib_number = line.split("\t")[0];
                    if (!bib_number.equals("?"))
                        bib_counts.put(bib_number, bib_counts.getOrDefault(bib_number, 0) + 1);
                }
    }

    private void processAnnotations(final Object path) {

        try {
            final List<String> lines = Files.readAllLines((Path) path);

            // Skip header line.
            for (int line_index = 1; line_index < lines.size(); line_index++) {

                final String[] elements = lines.get(line_index).split("\t");

                // May add insertion option later.
                if (elements[0].equals("Update"))
                    updateResult(raw_results, elements);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateResult(final List<? extends RawResult> raw_results, final String[] elements) {

        final int position = Integer.parseInt(elements[1]);
        final RawResult raw_result = raw_results.get(position - 1);

        if (elements[2].equals("?")) raw_result.setBibNumber(UNKNOWN_BIB_NUMBER);
        else if (!elements[2].isEmpty()) raw_result.setBibNumber(Integer.parseInt(elements[2]));

        if (elements[3].equals("?")) raw_result.setRecordedFinishTime(null);
        else if (!elements[3].isEmpty()) raw_result.setRecordedFinishTime(Normalisation.parseTime(elements[3]));

        if (!elements[4].isEmpty()) raw_result.appendComment(elements[4]);
    }

    private List<RaceEntry> loadEntries(final Path entries_path) throws IOException {

        return Files.readAllLines(entries_path).stream().
            map(Normalisation::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(line -> makeRelayRaceEntry(Arrays.stream(line.split("\t")).toList(), this)).
            toList();
    }

    private RaceEntry makeRelayRaceEntry(final List<String> elements, final Race2 race) {

        // Expected format: "1", "Team 1", "Women Senior", "John Smith", "Hailey Dickson & Alix Crawford", "Rhys Müllar & Paige Thompson", "Amé MacDonald"

        if (elements.size() != FIRST_RUNNER_NAME_INDEX + ((RelayRace) race).getNumberOfLegs())
            throw new RuntimeException("Invalid number of elements: " + String.join(" ", elements));

        try {
            final int bib_number = Integer.parseInt(elements.get(BIB_NUMBER_INDEX));

            final String name = elements.get(TEAM_NAME_INDEX);
            final EntryCategory category = race.getCategoryDetails().lookupEntryCategory(elements.get(CATEGORY_INDEX));
            final List<String> runners = elements.subList(FIRST_RUNNER_NAME_INDEX, elements.size()).stream().map(s -> race.getNormalisation().cleanRunnerName(s)).toList();

            final Participant participant = new Team(name, category, runners);

            return new RaceEntry(participant, bib_number, race);

        } catch (final RuntimeException e) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    @Override
    public RaceResultsCalculator getResultsCalculator() {
        return results_calculator;
    }

    @Override
    public void appendToNotes(final String note) {
        results_calculator.getNotes().append(note);
    }

    @Override
    public String getNotes() {
        return results_calculator.getNotes().toString();
    }

    @Override
    public synchronized Normalisation getNormalisation() {

        if (normalisation == null)
            normalisation = new Normalisation(this);

        return normalisation;
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

    public void setCategoriesProcessor(final CategoriesProcessor categories_processor) {

        this.categories_processor = categories_processor;
        categories_processor.setRace(this);
    }

    public void setResultsCalculator(final RaceResultsCalculator results_calculator) {

        this.results_calculator = results_calculator;
        results_calculator.setRace(this);
    }

    public void setResultsOutput(final ResultsOutput results_output) {

        this.results_output = results_output;
        results_output.setRace(this);
    }

    @Override
    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public List<RaceEntry> getEntries() {
        return entries;
    }

    /**
     * For each leg, records whether there was a mass start.
     */
    private List<Boolean> mass_start_legs;

    /**
     * For each leg, records whether it is a leg for paired runners.
     */
    private List<Boolean> paired_legs;

    /**
     * Times relative to start of leg 1 at which each mass start occurred.
     * For leg 2 onward, legs that didn't have a mass start are recorded with the time of the next actual
     * mass start. This allows e.g. for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
     */
    private List<Duration> start_times_for_mass_starts;

    /**
     * List of individually recorded starts (usually empty).
     */
    private List<IndividualStart> individual_starts;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void completeConfiguration() {
        throw new UnsupportedOperationException();
    }

    public void completeConfiguration2() {

        configureIndividualLegStarts();
        configureMassStarts();
        configurePairedLegs();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Packages details of an individually recorded leg start (unusual). */
    public record IndividualStart(int bib_number, int leg_number, Duration start_time) {
    }

    /**
     * The number of legs in the relay race.
     */
    public int getNumberOfLegs() {
        return (int) getConfig().get(KEY_NUMBER_OF_LEGS);
    }

    List<Duration> getStartTimesForMassStarts() {
        return start_times_for_mass_starts;
    }

    List<IndividualStart> getIndividualStarts() {
        return individual_starts;
    }

    public List<Boolean> getPairedLegs() {
        return paired_legs;
    }

    List<Boolean> getMassStartLegs() {
        return mass_start_legs;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<LegResult> getLegResults(final int leg_number) {

        final List<LegResult> results = results_calculator.getOverallResults().stream().
            map(result -> (RelayRaceResult) result).
            map(result -> result.getLegResult(leg_number)).
            sorted().
            toList();

        // Deal with dead heats in legs after the first.
        RaceResultsCalculator.setPositionStrings(results, leg_number > 1);

        return results;
    }

    public List<String> getLegDetails(final RelayRaceResult result) {

        final List<String> leg_details = new ArrayList<>();
        boolean all_previous_legs_completed = true;

        for (int leg = 1; leg <= getNumberOfLegs(); leg++) {

            final LegResult leg_result = result.getLegResult(leg);
            final boolean completed = leg_result.canComplete();

            final String leg_runner_names = ((Team)leg_result.getParticipant()).getRunnerNames().get(leg - 1);
            final String leg_mass_start_annotation = getMassStartAnnotation(leg_result, leg);
            final String leg_time = renderDuration(leg_result, DNF_STRING);
            final String split_time = completed && all_previous_legs_completed ? renderDuration(sumDurationsUpToLeg(result.getLegResults(), leg)) : DNF_STRING;

            leg_details.add(leg_runner_names + leg_mass_start_annotation);
            leg_details.add(leg_time);
            leg_details.add(split_time);

            if (!completed) all_previous_legs_completed = false;
        }

        return leg_details;
    }

    Map<Integer, Integer> countLegsFinishedPerTeam() {

        final Map<Integer, Integer> legs_finished_map = new HashMap<>();

        for (final RawResult result : raw_results)
            legs_finished_map.merge(result.getBibNumber(), 1, Integer::sum);

        return legs_finished_map;
    }

    List<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count) {

        return getUniqueBibNumbersRecorded().stream().
            flatMap(bib_number -> getBibNumbersWithMissingTimes(leg_finished_count, bib_number)).
            sorted().
            toList();
    }

    Set<Integer> getUniqueBibNumbersRecorded() {

        final Set<Integer> bib_numbers_recorded = new HashSet<>();

        for (final RawResult result : raw_results)
            bib_numbers_recorded.add(result.getBibNumber());
        bib_numbers_recorded.remove(UNKNOWN_BIB_NUMBER);

        return bib_numbers_recorded;
    }

    List<Duration> getTimesWithMissingBibNumbers() {

        final List<Duration> times_with_missing_bib_numbers = new ArrayList<>();

        for (final RawResult raw_result : raw_results)
            if (raw_result.getBibNumber() == UNKNOWN_BIB_NUMBER)
                times_with_missing_bib_numbers.add(raw_result.getRecordedFinishTime());

        return times_with_missing_bib_numbers;
    }

    /**
     * Offset between actual race start time, and the time at which timing started.
     * Usually this is zero. A positive value indicates that the race started before timing started.
     */
    Duration getStartOffset() {

        // TODO add to individual race.

        return (Duration) getConfig().get(KEY_START_OFFSET);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private String getMassStartAnnotation(final LegResult leg_result, final int leg_number) {

        // Adds e.g. "(M3)" after names of runner_names that started in leg 3 mass start.
        return leg_result.isInMassStart() ? " (M" + getNextMassStartLeg(leg_number) + ")" : "";
    }

    private int getNextMassStartLeg(final int leg_number) {

        return leg_number +
            (int) mass_start_legs.subList(leg_number - 1, getNumberOfLegs()).stream().
                filter(is_mass_start -> !is_mass_start).
                count();
    }

    private static Duration sumDurationsUpToLeg(final List<? extends LegResult> leg_results, final int leg_number) {

        return leg_results.stream().
            limit(leg_number).
            map(LegResult::duration).
            reduce(Duration.ZERO, Duration::plus);
    }

    private Stream<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count, final int bib_number) {

        final int number_of_legs_unfinished = getNumberOfLegs() - leg_finished_count.getOrDefault(bib_number, 0);

        return Stream.generate(() -> bib_number).limit(number_of_legs_unfinished);
    }

    private void configurePairedLegs() {

        final String paired_legs_string = config.getStringConfig(KEY_PAIRED_LEGS);

        // Example: PAIRED_LEGS = 2,3
        paired_legs = Stream.generate(() -> false)
            .limit(getNumberOfLegs())
            .collect(Collectors.toCollection(ArrayList::new));

        for (final String leg_number_as_string : paired_legs_string.split(","))
            paired_legs.set(Integer.parseInt(leg_number_as_string) - 1, true);
    }

    private void configureMassStarts() {

        start_times_for_mass_starts = new ArrayList<>();
        mass_start_legs = new ArrayList<>();

        for (int i = 0; i < getNumberOfLegs(); i++) {
            start_times_for_mass_starts.add(null);
            mass_start_legs.add(false);
        }

        setMassStartTimes();

        // If there is no mass start configured for legs 2 and above, use the first actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.
        setEmptyMassStartTimes();
    }

    private void setMassStartTimes() {

        final Consumer<Object> process_mass_start_times = value -> {

            // Example: MASS_START_ELAPSED_TIMES = 00:00:00,00:00:00,00:00:00,2:36:00
            final String mass_start_string = config.getStringConfig(KEY_MASS_START_ELAPSED_TIMES);

            if (mass_start_string != null) {
                final String[] mass_start_elapsed_times_strings = mass_start_string.split(",");

                for (final String bib_time_as_string : mass_start_elapsed_times_strings)
                    setMassStartTime(bib_time_as_string);
            }
        };

        config.processConfigIfPresent(KEY_MASS_START_ELAPSED_TIMES, process_mass_start_times);
    }

    private void setMassStartTime(final String bib_time_as_string) {

        final String[] split = bib_time_as_string.split("/");

        final int leg_number = Integer.parseInt(split[0]);
        final Duration mass_start_time = parseTime(split[1]);

        start_times_for_mass_starts.set(leg_number - 1, mass_start_time);
        mass_start_legs.set(leg_number - 1, !mass_start_time.equals(VERY_LONG_DURATION));
    }

    private void setEmptyMassStartTimes() {

        // For legs 2 and above, if there is no mass start time configured, use the next actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.

        if (start_times_for_mass_starts.get(getNumberOfLegs() - 1) == null)
            start_times_for_mass_starts.set(getNumberOfLegs() - 1, VERY_LONG_DURATION);

        for (int leg_index = getNumberOfLegs() - 2; leg_index > 0; leg_index--)

            if (start_times_for_mass_starts.get(leg_index) == null)
                start_times_for_mass_starts.set(leg_index, start_times_for_mass_starts.get(leg_index + 1));
    }

    private void configureIndividualLegStarts() {

        final String individual_leg_starts_string = config.getStringConfig(KEY_INDIVIDUAL_LEG_STARTS);

        // bib number / leg number / start time
        // Example: INDIVIDUAL_LEG_STARTS = 2/1/0:10:00,26/3/2:41:20

        individual_starts = individual_leg_starts_string == null ? new ArrayList<>() :
            Arrays.stream(individual_leg_starts_string.split(",")).
                map(RelayRace::getIndividualLegStart).
                toList();
    }

    private static IndividualStart getIndividualLegStart(final String individual_leg_starts_string) {

        final String[] split = individual_leg_starts_string.split("/");

        final int bib_number = Integer.parseInt(split[0]);
        final int leg_number = Integer.parseInt(split[1]);
        final Duration start_time = parseTime(split[2]);

        return new IndividualStart(bib_number, leg_number, start_time);
    }
}
