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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.output.RaceOutput.DNF_STRING;

public class RelayRace extends SingleRace {

    // Configuration file keys.
    private static final String KEY_GENDER_ELIGIBILITY_MAP_PATH = "GENDER_ELIGIBILITY_MAP_PATH";
    private static final String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    private static final String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    private static final String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    private static final String KEY_MASS_START_ELAPSED_TIMES = "MASS_START_ELAPSED_TIMES";
    private static final String KEY_START_OFFSET = "START_OFFSET";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    int getNumberOfLegs() {
        return number_of_legs;
    }

    Set<Integer> getPairedLegs() {
        return paired_legs;
    }

    record LegOutputInfo(String leg_runner_names, String leg_mass_start_annotation,
                         String leg_time, String split_time) {
    }

    private record IndividualLegStart(int bib_number, int leg_number, Duration start_time) {
    }

    private record ResultWithLegIndex(RelayRaceResult result, int leg_index) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private int number_of_legs;
    private RelayRaceMissingData missing_data;

    // For each leg, records whether there was a mass start.
    private List<Boolean> mass_start_legs;

    // Times relative to start of leg 1 at which each mass start occurred.
    // For leg 2 onward, legs that didn't have a mass start are recorded with the time of the next actual
    // mass start. This allows e.g. for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
    private List<Duration> start_times_for_mass_starts;

    private Set<Integer> paired_legs;
    private List<IndividualLegStart> individual_leg_starts;
    private Duration start_offset;
    private Map<String, String> gender_eligibility_map;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java RelayRace <config file path>");
        else {
            new RelayRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void calculateResults() {

        initialiseResults();

        interpolateMissingTimes();
        guessMissingBibNumbers();

        fillFinishTimes();
        fillStartTimes();
        fillDNFs();

        sortResults();
        allocatePrizes();

        addPaperRecordingComments();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        super.configure();

        configureMassStarts();
        configurePairedLegs();
        configureIndividualLegStarts();
        configureGenderEligibilityMap();
    }

    @Override
    protected void readProperties() throws IOException {

        super.readProperties();

        number_of_legs = Integer.parseInt(getProperty(KEY_NUMBER_OF_LEGS));
        start_offset = parseTime(getProperty(KEY_START_OFFSET, format(Duration.ZERO)));
    }

    @Override
    protected void configureInputData() throws IOException {

        super.configureInputData();

        ((RelayRaceInput) input).loadTimeAnnotations(raw_results);
    }

    @Override
    protected RaceInput getInput() {
        return new RelayRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new RelayRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return new RelayRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new RelayRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new RelayRaceOutputPDF(this);
    }

    @Override
    protected void configureHelpers() {

        super.configureHelpers();

        missing_data = new RelayRaceMissingData(this);
        prizes = new RelayRacePrizes(this);
    }

    @Override
    protected void outputResults() throws IOException {

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printCollatedTimes();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {

        // Sort in order of increasing overall team time, as defined in OverallResult.compareTo().
        // DNF results are sorted in increasing order of bib number.
        // Where two teams have the same overall time, the order in which their last leg runner_names were recorded is preserved.

        return List.of(Race::compareCompletion, Race::comparePerformance, this::compareLastLegPosition);
    }

    @Override
    protected List<Comparator<RaceResult>> getDNFComparators() {

        return List.of(RelayRace::compareBibNumber);
    }

    @Override
    protected boolean isEntryCategoryEligibleForPrizeCategoryByGender(final EntryCategory entry_category, final PrizeCategory prize_category) {

        if (entry_category.getGender().equals(prize_category.getGender())) return true;

        return gender_eligibility_map.keySet().stream().
            filter(entry_gender -> entry_category.getGender().equals(entry_gender)).
            anyMatch(entry_gender -> prize_category.getGender().equals(gender_eligibility_map.get(entry_gender)));
    }

    @Override
    protected EntryCategory getEntryCategory(final RaceResult result) {
        return ((RelayRaceResult) result).entry.team.category();
    }

    @Override
    protected void fillDNF(final String individual_dnf_string) {

        try {
            final ResultWithLegIndex result_with_leg = getResultWithLegIndex(individual_dnf_string);
            final LegResult result = result_with_leg.result.leg_results.get(result_with_leg.leg_index);

            result.completion_status = CompletionStatus.DNF;

        } catch (final RuntimeException _) {
            throw new RuntimeException("illegal DNF time");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        entries.forEach(entry -> overall_results.add(new RelayRaceResult((RelayRaceEntry) entry, this)));
    }

    private List<Comparator<RaceResult>> getLegResultComparators(final int leg_number) {

        return leg_number == 1 ?
            List.of(Race::compareCompletion, Race::comparePerformance, this::compareRecordedLegPosition) :
            List.of(Race::compareCompletion, Race::comparePerformance, Race::compareRunnerLastName, Race::compareRunnerFirstName);
    }

    private static int compareBibNumber(final RaceResult r1, final RaceResult r2) {

        return Integer.compare(((RelayRaceResult) r1).entry.bib_number, ((RelayRaceResult) r2).entry.bib_number);
    }

    private int compareLastLegPosition(final RaceResult r1, final RaceResult r2) {

        return Integer.compare(getRecordedLastLegPosition(((RelayRaceResult) r1)), getRecordedLastLegPosition(((RelayRaceResult) r2)));
    }

    private int compareRecordedLegPosition(final RaceResult r1, final RaceResult r2) {

        final int leg_number = ((LegResult) r1).leg_number;

        final int recorded_position1 = getRecordedLegPosition(((LegResult) r1).entry.bib_number, leg_number);
        final int recorded_position2 = getRecordedLegPosition(((LegResult) r2).entry.bib_number, leg_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    List<LegResult> getLegResults(final int leg_number) {

        final List<LegResult> results = getOverallResults().stream().
            map(result -> (RelayRaceResult) result).
            map(result -> result.leg_results.get(leg_number - 1)).
            sorted(combineComparators(getLegResultComparators(leg_number))).
            toList();

        // Deal with dead heats in legs after the first.
        setPositionStrings(results, leg_number > 1);

        return results;
    }

    private String getMassStartAnnotation(final LegResult leg_result, final int leg_number) {

        // Adds e.g. "(M3)" after names of runner_names that started in leg 3 mass start.
        return leg_result.in_mass_start ? STR." (M\{getNextMassStartLeg(leg_number)})" : "";
    }

    private int getNextMassStartLeg(final int leg_number) {

        return leg_number +
            (int) mass_start_legs.subList(leg_number - 1, number_of_legs).stream().
                filter(is_mass_start -> !is_mass_start).
                count();
    }

    private static Duration sumDurationsUpToLeg(final List<? extends LegResult> leg_results, final int leg_number) {

        return leg_results.subList(0, leg_number).stream().
            map(LegResult::duration).
            reduce(Duration::plus).
            orElse(Duration.ZERO);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private int getRecordedLegPosition(final int bib_number, final int leg_number) {

        final AtomicInteger legs_completed = new AtomicInteger(0);

        final int position = (int) raw_results.stream().
            peek(result -> {
                if (result.getBibNumber() == bib_number) legs_completed.incrementAndGet();
            }).
            takeWhile(result -> result.getBibNumber() != bib_number || legs_completed.get() < leg_number).
            count() + 1;

        return position <= raw_results.size() ? position : UNKNOWN_RACE_POSITION;
    }

    private int getRecordedLastLegPosition(final RelayRaceResult result) {

        return getRecordedLegPosition(result.entry.bib_number, number_of_legs);
    }

    private void interpolateMissingTimes() {

        missing_data.interpolateMissingTimes();
    }

    private void guessMissingBibNumbers() {

        missing_data.guessMissingBibNumbers();
    }

    private void configureMassStarts() {

        start_times_for_mass_starts = new ArrayList<>();
        mass_start_legs = new ArrayList<>();

        setMassStartTimes();

        // If there is no mass start configured for legs 2 and above, use the first actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.
        setEmptyMassStartTimes();
    }

    private String getMassStartElapsedTimesString() {

        // Example: MASS_START_ELAPSED_TIMES = 00:00:00,00:00:00,00:00:00,2:36:00

        final String default_string = Stream.generate(() -> format(Duration.ZERO)).
            limit(number_of_legs).
            collect(Collectors.joining(","));

        return getProperty(KEY_MASS_START_ELAPSED_TIMES, default_string);
    }

    private void setMassStartTimes() {

        final String[] mass_start_elapsed_times_strings = getMassStartElapsedTimesString().split(",");

        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            setMassStartTime(mass_start_elapsed_times_strings[leg_index], leg_index);
    }

    private void setMassStartTime(final String time_as_string, final int leg_index) {

        final Duration mass_start_time = parseTime(time_as_string);
        final Duration previous_mass_start_time = leg_index > 0 ? start_times_for_mass_starts.get(leg_index - 1) : null;

        verifyMassStartTimesOrder(previous_mass_start_time, mass_start_time);

        start_times_for_mass_starts.add(mass_start_time);
        mass_start_legs.add(!mass_start_time.equals(Duration.ZERO));
    }

    @SuppressWarnings({"TypeMayBeWeakened", "IfCanBeAssertion"})
    private static void verifyMassStartTimesOrder(final Duration previous_mass_start_time, final Duration current_mass_start_time) {

        if (previous_mass_start_time != null &&
            !previous_mass_start_time.equals(Duration.ZERO) &&
            previous_mass_start_time.compareTo(current_mass_start_time) > 0)

            throw new RuntimeException("illegal mass start time order");
    }

    private void setEmptyMassStartTimes() {

        // For legs 2 and above, if there is no mass start time configured, use the next actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.

        for (int leg_index = number_of_legs - 2; leg_index > 0; leg_index--)

            if (start_times_for_mass_starts.get(leg_index).equals(Duration.ZERO))
                start_times_for_mass_starts.set(leg_index, start_times_for_mass_starts.get(leg_index + 1));
    }

    private void configureGenderEligibilityMap() throws IOException {

        final String gender_eligibility_map_path = getProperty(KEY_GENDER_ELIGIBILITY_MAP_PATH);

        gender_eligibility_map = new HashMap<>();

        Files.readAllLines(getPath(gender_eligibility_map_path)).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(line -> {
                final String[] elements = line.split(",");
                gender_eligibility_map.put(elements[0], elements[1]);
            });
    }

    private void configurePairedLegs() {

        final String paired_legs_string = getProperty(KEY_PAIRED_LEGS);

        // Example: PAIRED_LEGS = 2,3

        paired_legs = new HashSet<>();

        for (final String leg_number_as_string : paired_legs_string.split(","))
            paired_legs.add(Integer.parseInt(leg_number_as_string));
    }

    private void configureIndividualLegStarts() {

        final String individual_leg_starts_string = getProperty(KEY_INDIVIDUAL_LEG_STARTS);

        // bib number / leg number / start time
        // Example: INDIVIDUAL_LEG_STARTS = 2/1/0:10:00,26/3/2:41:20

        individual_leg_starts = individual_leg_starts_string == null ? new ArrayList<>() :
            Arrays.stream(individual_leg_starts_string.split(",")).
                map(RelayRace::getIndividualLegStart).
                toList();
    }

    private static IndividualLegStart getIndividualLegStart(final String individual_leg_starts_string) {

        final String[] split = individual_leg_starts_string.split("/");

        final int bib_number = Integer.parseInt(split[0]);
        final int leg_number = Integer.parseInt(split[1]);
        final Duration start_time = parseTime(split[2]);

        return new IndividualLegStart(bib_number, leg_number, start_time);
    }

    private void fillFinishTimes() {

        recordLegResults();
        sortLegResults();
    }

    private void recordLegResults() {

        raw_results.stream().
            filter(result -> result.getBibNumber() != UNKNOWN_BIB_NUMBER).
            forEachOrdered(result -> recordLegResult((RelayRaceRawResult) result));
    }

    private void recordLegResult(final RelayRaceRawResult raw_result) {

        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult) overall_results.get(team_index);

        final int leg_index = findIndexOfNextUnfilledLegResult(result.leg_results);
        final LegResult leg_result = result.leg_results.get(leg_index);

        leg_result.finish_time = raw_result.getRecordedFinishTime().plus(start_offset);

        // Leg number will be zero in most cases, unless explicitly recorded in raw results.
        leg_result.leg_number = raw_result.getLegNumber();

        // Provisionally this leg is not DNF since a finish time was recorded.
        // However, it might still be set to DNF in fillDNFs() if the runner missed a checkpoint.
        leg_result.completion_status = CompletionStatus.COMPLETED;
    }

    private void sortLegResults() {

        overall_results.forEach(RelayRace::sortLegResults);
    }

    private static void sortLegResults(final RaceResult result) {

        final List<LegResult> leg_results = ((RelayRaceResult) result).leg_results;

        // Sort by explicitly recorded leg number.
        leg_results.sort(Comparator.comparingInt(o -> o.leg_number));

        // Reset the leg numbers according to new positions in leg sequence.
        for (int leg_index = 1; leg_index <= leg_results.size(); leg_index++)
            leg_results.get(leg_index - 1).leg_number = leg_index;
    }

    private ResultWithLegIndex getResultWithLegIndex(final String bib_and_leg) {

        // String of form "bib-number/leg-number"

        final String[] elements = bib_and_leg.split("/");
        final int bib_number = Integer.parseInt(elements[0]);
        final int leg_number = Integer.parseInt(elements[1]);

        final RelayRaceResult result = (RelayRaceResult) overall_results.get(findIndexOfTeamWithBibNumber(bib_number));

        return new ResultWithLegIndex(result, leg_number - 1);
    }

    private void fillStartTimes() {

        overall_results.forEach(this::fillLegResultDetails);
    }

    private void fillLegResultDetails(final RaceResult result) {

        for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
            fillLegResultDetails(((RelayRaceResult) result).leg_results, leg_index);
    }

    private void fillLegResultDetails(final List<? extends LegResult> leg_results, final int leg_index) {

        final LegResult leg_result = leg_results.get(leg_index);

        final Duration individual_start_time = getIndividualStartTime(leg_result, leg_index);
        final Duration leg_mass_start_time = start_times_for_mass_starts.get(leg_index);
        final Duration previous_team_member_finish_time = leg_index > 0 ? leg_results.get(leg_index - 1).finish_time : null;

        leg_result.start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);

        // Record whether the runner started in a mass start.
        leg_result.in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);
    }

    private Duration getIndividualStartTime(final LegResult leg_result, final int leg_index) {

        return individual_leg_starts.stream().
            filter(individual_leg_start -> individual_leg_start.bib_number == leg_result.entry.bib_number).
            filter(individual_leg_start -> individual_leg_start.leg_number == leg_index + 1).
            map(individual_leg_start -> individual_leg_start.start_time).
            findFirst().
            orElse(null);
    }

    List<String> getLegDetails(final RelayRaceResult result, final Function<? super LegOutputInfo, String> leg_output_formatter) {

        final List<String> leg_details = new ArrayList<>();
        boolean all_previous_legs_completed = true;

        for (int leg = 1; leg <= number_of_legs; leg++) {

            final LegResult leg_result = result.leg_results.get(leg - 1);
            final boolean completed = leg_result.getCompletionStatus() == CompletionStatus.COMPLETED;

            final String leg_runner_names = leg_result.entry.team.runner_names().get(leg - 1);
            final String leg_mass_start_annotation = getMassStartAnnotation(leg_result, leg);
            final String leg_time = completed ? format(leg_result.duration()) : DNF_STRING;
            final String split_time = completed && all_previous_legs_completed ? format(sumDurationsUpToLeg(result.leg_results, leg)) : DNF_STRING;

            leg_details.add(leg_output_formatter.apply(new LegOutputInfo(leg_runner_names, leg_mass_start_annotation, leg_time, split_time)));

            if (!completed) all_previous_legs_completed = false;
        }

        return leg_details;
    }

    private static Duration getLegStartTime(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_team_member_finish_time, final int leg_index) {

        // Individual leg time recorded for this runner.
        if (individual_start_time != null) return individual_start_time;

        // Leg 1 runner_names start at time zero if there's no individual time recorded.
        if (leg_index == 0) return Duration.ZERO;

        // No finish time recorded for previous runner, so we can't record a start time for this one.
        // This leg result will be set to DNF by default.
        if (previous_team_member_finish_time == null) return null;

        // Use the earlier of the mass start time and the previous runner's finish time.
        return !mass_start_time.equals(Duration.ZERO) && mass_start_time.compareTo(previous_team_member_finish_time) < 0 ? mass_start_time : previous_team_member_finish_time;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private boolean isInMassStart(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // Not in mass start if there is an individually recorded time, or it's the first leg.
        if (individual_start_time != null || leg_index == 0) return false;

        // No previously recorded leg time, so record this runner as starting in mass start if it's a mass start leg.
        if (previous_runner_finish_time == null) return mass_start_legs.get(leg_index);

        // Record this runner as starting in mass start if the previous runner finished after the relevant mass start.
        return !mass_start_time.equals(Duration.ZERO) && mass_start_time.compareTo(previous_runner_finish_time) < 0;
    }

    @SuppressWarnings({"TypeMayBeWeakened", "IfCanBeAssertion"})
    private static int findIndexOfNextUnfilledLegResult(final List<? extends LegResult> leg_results) {

        final int index = (int) leg_results.stream().
            takeWhile(result -> result.finish_time != null).
            count();

        if (index == leg_results.size())
            throw new RuntimeException(STR."surplus result recorded for team: \{leg_results.getFirst().entry.bib_number}");

        return index;
    }

    @SuppressWarnings({"IfCanBeAssertion"})
    private int findIndexOfTeamWithBibNumber(final int bib_number) {

        final int index = (int) overall_results.stream().
            map(result -> (RelayRaceResult) result).
            takeWhile(result -> result.entry.bib_number != bib_number).
            count();

        if (index == overall_results.size()) throw new RuntimeException(STR."unregistered team: \{bib_number}");

        return index;
    }

    private void addPaperRecordingComments() {

        for (int i = 0; i < raw_results.size(); i++) {

            final boolean last_electronically_recorded_result = i == ((RelayRaceInput) input).getNumberOfRawResults() - 1;

            if (last_electronically_recorded_result && ((RelayRaceInput) input).getNumberOfRawResults() < raw_results.size())
                raw_results.get(i).appendComment("Remaining times from paper recording sheet only.");
        }
    }

    private void printDetailedResults() throws IOException {

        ((RelayRaceOutputCSV) output_CSV).printDetailedResults();
        ((RelayRaceOutputHTML) output_HTML).printDetailedResults();
    }

    private void printLegResults() throws IOException {

        ((RelayRaceOutputCSV) output_CSV).printLegResults();
        ((RelayRaceOutputHTML) output_HTML).printLegResults();
    }

    private void printCollatedTimes() throws IOException {

        ((RelayRaceOutputText) output_text).printCollatedResults();
    }
}
