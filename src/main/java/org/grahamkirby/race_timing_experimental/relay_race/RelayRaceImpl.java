/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.relay_race;

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing_experimental.common.*;
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceResultsCalculatorImpl;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;
import static org.grahamkirby.race_timing.common.output.RaceOutput.DNF_STRING;
import static org.grahamkirby.race_timing_experimental.common.Config.*;
import static org.grahamkirby.race_timing_experimental.relay_race.RelayRaceResultsCalculatorImpl.*;

public class RelayRaceImpl implements SpecificRace {

    private Race race;
    private Map<Integer, Integer> legs_finished_per_team;

    public void setRace(Race race) {
        this.race = race;

    }

    @Override
    public void completeConfiguration() {
        configureIndividualLegStarts();
        configureMassStarts();
        configurePairedLegs();
        configureLegsFinishedPerTeam();
    }

    List<LegResult> getLegResults(final int leg_number) {

        final List<LegResult> results = race.getResultsCalculator().getOverallResults().stream().
            map(result -> (RelayRaceResult) result).
            map(result -> result.leg_results.get(leg_number - 1)).
            sorted(combineComparators(getLegResultComparators(leg_number))).
            toList();

        // Deal with dead heats in legs after the first.
        setPositionStrings(results, leg_number > 1);

        return results;
    }


    private List<Comparator<RaceResult>> getLegResultComparators(final int leg_number) {

        return leg_number == 1 ?
            List.of(
                ignoreIfBothResultsAreDNF(penaliseDNF(IndividualRaceResultsCalculatorImpl::comparePerformance)),
                this::compareRecordedLegPosition) :
            List.of(
                ignoreIfBothResultsAreDNF(penaliseDNF(IndividualRaceResultsCalculatorImpl::comparePerformance)),
                RelayRaceImpl::compareLastNameOfFirstRunner, RelayRaceImpl::compareFirstNameOfFirstRunner);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    protected static int compareFirstNameOfFirstRunner(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getFirstNameOfFirstRunner(r1.getParticipantName()).compareTo(Normalisation.getFirstNameOfFirstRunner(r2.getParticipantName()));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    protected static int compareLastNameOfFirstRunner(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getLastNameOfFirstRunner(r1.getParticipantName()).compareTo(Normalisation.getLastNameOfFirstRunner(r2.getParticipantName()));
    }

    private int compareRecordedLegPosition(final RaceResult r1, final RaceResult r2) {

        final int leg_number = ((LegResult) r1).leg_number;

        final int recorded_position1 = getRecordedLegPosition(((SingleRaceResult)r1).entry.bib_number, leg_number);
        final int recorded_position2 = getRecordedLegPosition(((SingleRaceResult) r2).entry.bib_number, leg_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    private int getRecordedLegPosition(final int bib_number, final int leg_number) {

        final AtomicInteger legs_completed = new AtomicInteger(0);

        return (int) race.getRaceData().getRawResults().stream().
            peek(result -> {
                if (result.getBibNumber() == bib_number) legs_completed.incrementAndGet();
            }).
            takeWhile(result -> result.getBibNumber() != bib_number || legs_completed.get() < leg_number).
            count() + 1;
    }

    public List<String> getLegDetails(RelayRaceResult result) {

        final List<String> leg_details = new ArrayList<>();
        boolean all_previous_legs_completed = true;

        for (int leg = 1; leg <= getNumberOfLegs(); leg++) {

            final LegResult leg_result = result.leg_results.get(leg - 1);
            final boolean completed = leg_result.canComplete();

            final String leg_runner_names = ((Team)leg_result.entry.participant).runner_names.get(leg - 1);
            final String leg_mass_start_annotation = getMassStartAnnotation(leg_result, leg);
            final String leg_time = renderDuration(leg_result, DNF_STRING);
            final String split_time = completed && all_previous_legs_completed ? format(sumDurationsUpToLeg(result.leg_results, leg)) : DNF_STRING;

            leg_details.add(leg_runner_names + leg_mass_start_annotation);
            leg_details.add(leg_time);
            leg_details.add(split_time);

            if (!completed) all_previous_legs_completed = false;
        }

        return leg_details;
    }

    public static String renderDuration(final RaceResult result, final String alternative) {

        if (!result.canComplete()) return alternative;

        final Duration duration = ((SingleRaceResult)result).duration();

        return format(duration);
    }
    private String getMassStartAnnotation(final LegResult leg_result, final int leg_number) {

        // Adds e.g. "(M3)" after names of runner_names that started in leg 3 mass start.
        return leg_result.in_mass_start ? STR." (M\{getNextMassStartLeg(leg_number)})" : "";
    }

    private int getNextMassStartLeg(final int leg_number) {

        return leg_number +
            (int) mass_start_legs.subList(leg_number - 1, getNumberOfLegs()).stream().
                filter(is_mass_start -> !is_mass_start).
                count();
    }

    public static Duration sumDurationsUpToLeg(final List<? extends LegResult> leg_results, final int leg_number) {

        return leg_results.stream().
            limit(leg_number).
            map(LegResult::duration).
            reduce(Duration.ZERO, Duration::plus);
    }


    public Map<Integer, Integer> countLegsFinishedPerTeam() {
//        return legs_finished_per_team;

        final Map<Integer, Integer> legs_finished_map = new HashMap<>();

        for (final RawResult result : race.getRaceData().getRawResults())
            legs_finished_map.merge(result.getBibNumber(), 1, Integer::sum);

        return legs_finished_map;

    }

    private void configureLegsFinishedPerTeam() {

//        final Map<Integer, Integer> legs_finished_map = new HashMap<>();
//
//        for (final RawResult result : race.getRaceData().getRawResults())
//            legs_finished_map.merge(result.getBibNumber(), 1, Integer::sum);
//
//        legs_finished_per_team = legs_finished_map;
    }

    List<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count) {

        return getUniqueBibNumbersRecorded().stream().
            flatMap(bib_number -> getBibNumbersWithMissingTimes(leg_finished_count, bib_number)).
            sorted().
            toList();
    }
    public Set<Integer> getUniqueBibNumbersRecorded() {
        final Set<Integer> bib_numbers_recorded = new HashSet<>();
        for (RawResult result : race.getRaceData().getRawResults())
            bib_numbers_recorded.add(result.getBibNumber());
        bib_numbers_recorded.remove(UNKNOWN_BIB_NUMBER);
        return bib_numbers_recorded;
    }
    private Stream<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count, final int bib_number) {

//        final int bib_number = entry.bib_number;
        final int number_of_legs_unfinished = ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs() - leg_finished_count.getOrDefault(bib_number, 0);

        return Stream.generate(() -> bib_number).limit(number_of_legs_unfinished);
    }


    List<Duration> getTimesWithMissingBibNumbers() {

        final List<Duration> times_with_missing_bib_numbers = new ArrayList<>();

        for (final RawResult raw_result : race.getRaceData().getRawResults()) {

            if (raw_result.getBibNumber() == UNKNOWN_BIB_NUMBER)
                times_with_missing_bib_numbers.add(raw_result.getRecordedFinishTime());
        }

        return times_with_missing_bib_numbers;
    }

    /** Packages details of an individually recorded leg start (unusual). */
    public record IndividualStart(int bib_number, int leg_number, Duration start_time) {
    }

    /**
     * The number of legs in the relay race.
     * Value is read from configuration file using key KEY_NUMBER_OF_LEGS.
     */
    public int getNumberOfLegs() {
        return (int) race.getConfig().get(KEY_NUMBER_OF_LEGS);
    }

    /**
     * For each leg, records whether there was a mass start.
     * Values are read from configuration file using key KEY_MASS_START_ELAPSED_TIMES.
     */
    private List<Boolean> mass_start_legs;

    public List<Boolean> getMassStartLegs() {
        return mass_start_legs;
    }

    /**
     * For each leg, records whether it is a leg for paired runners.
     * Values are read from configuration file using key KEY_PAIRED_LEGS.
     */
    private List<Boolean> paired_legs;

    public List<Boolean> getPairedLegs() {
        return paired_legs;
    }

    private void configurePairedLegs() {

        final String paired_legs_string = (String) race.getConfig().get(KEY_PAIRED_LEGS);

        // Example: PAIRED_LEGS = 2,3
        paired_legs = Stream.generate(() -> false)
            .limit(getNumberOfLegs())
            .collect(Collectors.toCollection(ArrayList::new));

        for (final String leg_number_as_string : paired_legs_string.split(","))
            paired_legs.set(Integer.parseInt(leg_number_as_string) - 1, true);
    }

    /**
     * Times relative to start of leg 1 at which each mass start occurred.
     * For leg 2 onward, legs that didn't have a mass start are recorded with the time of the next actual
     * mass start. This allows e.g. for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
     *
     * Values are read from configuration file using key KEY_MASS_START_ELAPSED_TIMES.
     */
    private List<Duration> start_times_for_mass_starts;

    public List<Duration> getStartTimesForMassStarts() {
        return start_times_for_mass_starts;
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

        String s = (String) race.getConfig().get(KEY_MASS_START_ELAPSED_TIMES);
        if (s != null) return s;

        return Stream.generate(() -> format(Duration.ZERO)).
            limit(getNumberOfLegs()).
            collect(Collectors.joining(","));
    }

    private void setMassStartTimes() {

        final String[] mass_start_elapsed_times_strings = getMassStartElapsedTimesString().split(",");

        for (int leg_index = 0; leg_index < getNumberOfLegs(); leg_index++)
            setMassStartTime(mass_start_elapsed_times_strings[leg_index]);
    }

    private void setMassStartTime(final String time_as_string) {

        final Duration mass_start_time = parseTime(time_as_string);

        start_times_for_mass_starts.add(mass_start_time);
        mass_start_legs.add(!mass_start_time.equals(Duration.ZERO));
    }

    private void setEmptyMassStartTimes() {

        // For legs 2 and above, if there is no mass start time configured, use the next actual mass start time.
        // This covers the case where an early leg runner finishes after a mass start.

        for (int leg_index = getNumberOfLegs() - 2; leg_index > 0; leg_index--)

            if (start_times_for_mass_starts.get(leg_index).equals(Duration.ZERO))
                start_times_for_mass_starts.set(leg_index, start_times_for_mass_starts.get(leg_index + 1));
    }

    /**
     * List of individually recorded starts (usually empty).
     * Values are read from configuration file using key KEY_INDIVIDUAL_LEG_STARTS.
     */
    private List<IndividualStart> individual_starts;

    public List<IndividualStart> getIndividualStarts() {
        return individual_starts;
    }

    private void configureIndividualLegStarts() {

        final String individual_leg_starts_string = (String) race.getConfig().get(KEY_INDIVIDUAL_LEG_STARTS);

        // bib number / leg number / start time
        // Example: INDIVIDUAL_LEG_STARTS = 2/1/0:10:00,26/3/2:41:20

        individual_starts = individual_leg_starts_string == null ? new ArrayList<>() :
            Arrays.stream(individual_leg_starts_string.split(",")).
                map(RelayRaceImpl::getIndividualLegStart).
                toList();
    }

    private static IndividualStart getIndividualLegStart(final String individual_leg_starts_string) {

        final String[] split = individual_leg_starts_string.split("/");

        final int bib_number = Integer.parseInt(split[0]);
        final int leg_number = Integer.parseInt(split[1]);
        final Duration start_time = parseTime(split[2]);

        return new IndividualStart(bib_number, leg_number, start_time);
    }

    /**
     * Offset between actual race start time, and the time at which timing started.
     * Usually this is zero. A positive value indicates that the race started before timing started.
     *
     * Value is read from configuration file using key KEY_START_OFFSET.
     */
    Duration getStartOffset() {

        return (Duration) race.getConfig().get(KEY_START_OFFSET);
    }
}
