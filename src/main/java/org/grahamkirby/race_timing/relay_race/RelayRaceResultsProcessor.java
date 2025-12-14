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

import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static org.grahamkirby.race_timing.common.Config.*;

public class RelayRaceResultsProcessor extends RaceResultsProcessor implements RelayRaceResults {

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Calculations specific to a relay race with individual and paired legs. Features/assumptions:
    //
    //     * There are various team categories.
    //     * Each leg is run by either one or two runners.
    //     * Some result times may be missing, in which case they are interpolated where possible.
    //     * Some result bib numbers may be missing, in which case they are guessed where possible.
    //     * Additional results can be imported from paper records.
    //     * Additional annotations can be imported:
    //         * Overriding bib numbers or times for particular positions.
    //     * A leg after the first leg may have a mass start, at which all remaining runners on that leg start together.
    //     * There can be dead heats in overall results, and in legs after the first.
    //     * Team gender category can be all women, mixed (half women, half men) or open (no restrictions).
    //     * Any team can win a prize in an open category.
    //     * Team age categories can overlap, so that for example a 50+ team can win a prize in a 40+ category.
    //     * First place in an older category is awarded in preference to a lower prize in a lower category.
    //     * Separate results are calculated for each leg.
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record TeamSummaryAtPosition(int team_number, int finishes_before, int finishes_after,
                                         Duration previous_finish, Duration next_finish) {
    }

    private record ContiguousSequence(int start_index, int end_index) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int HALF_A_SECOND_IN_NANOSECONDS = 500_000_000;
    private static final int UNKNOWN_LEG_NUMBER = 0;

    private static final List<Comparator<TeamSummaryAtPosition>> team_summary_comparators = List.of(

        comparingInt(o -> o.finishes_before),
        comparingInt(o -> o.finishes_after),
        Comparator.comparing(o -> o.next_finish),
        Comparator.comparing(o -> o.previous_finish)
    );

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRaceResultsProcessor(final RaceInternal race) {

        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void calculateResults() {

        initialiseResults();
        guessMissingData();

        recordFinishTimes();
        fillLegResultDetails();
        recordDNFs();

        sortOverallResults();
        allocatePrizes();

        addPaperRecordingComments();
    }

    @Override
    public boolean canDistinguishFromOtherEqualPerformances(final RaceResult result) {

        // Dead heats are allowed in overall results, since each overall duration is composed of multiple
        // leg durations.
        //
        // Each leg duration is rounded to the nearest second, so the actual overall duration could be
        // anywhere in the range (calculated +/- (number of legs)/2). This means there's no way to
        // distinguish two equal overall results.

        // Can't be distinguished if any leg result was in mass start, or if last leg finish was after last recorded
        // finish, so time can't be properly interpolated.
        return ((RelayRaceResult) result).getLegResults().stream().
            noneMatch(r -> r.isInMassStart() || r.isFinishTimeUnknown());
    }

    @Override
    protected void recordDNF(final String dnf_specification) {

        try {
            // String of form "bib-number/leg-number"

            final String[] elements = dnf_specification.split("/");
            final int bib_number = Integer.parseInt(elements[0]);
            final int leg_number = Integer.parseInt(elements[1]);

            getLegResult(bib_number, leg_number).setDnf(true);

        } catch (final NumberFormatException e) {
            throw new RuntimeException(dnf_specification, e);
        }
    }

    @Override
    public List<? extends RawResult> getRawResults() {
        return ((RelayRace) race).getRawResults();
    }

    @Override
    public int getNumberOfLegs() {
        return ((RelayRace) race).getNumberOfLegs();
    }

    @Override
    public List<RelayRaceLegResult> getLegResults(final int leg) {
        return ((RelayRace) race).getLegResults(leg);
    }

    @Override
    public List<String> getLegDetails(final RelayRaceResult result) {
        return ((RelayRace) race).getLegDetails(result);
    }

    @Override
    public List<Boolean> getPairedLegs() {
        return ((RelayRace) race).getPairedLegs();
    }

    @Override
    public Map<Integer, Integer> countLegsFinishedPerTeam() {
        return ((RelayRace) race).countLegsFinishedPerTeam();
    }

    @Override
    public Map<RawResult, Integer> getExplicitlyRecordedLegNumbers() {
        return ((RelayRace) race).getExplicitlyRecordedLegNumbers();
    }

    @Override
    public List<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count) {
        return ((RelayRace) race).getBibNumbersWithMissingTimes(leg_finished_count);
    }

    @Override
    public List<Duration> getTimesWithMissingBibNumbers() {
        return ((RelayRace) race).getTimesWithMissingBibNumbers();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        final Collection<Integer> bib_numbers_seen = new HashSet<>();

        final List<RaceResult> results = ((SingleRaceInternal) race).getRawResults().stream().
            filter(raw_result -> raw_result.getBibNumber() != 0).
            filter(raw_result -> bib_numbers_seen.add(raw_result.getBibNumber())).
            map(this::makeRaceResult).
            toList();

        overall_results = makeMutableCopy(results);
    }

    private void guessMissingData() {

        interpolateMissingTimes();
        guessMissingBibNumbers();
    }

    private void recordFinishTimes() {

        recordLegResults();
        sortLegResults();
    }

    private void recordLegResults() {

        ((SingleRaceInternal) race).getRawResults().stream().
            filter(result -> result.getBibNumber() != UNKNOWN_BIB_NUMBER).
            forEachOrdered(this::recordLegResult);
    }

    private void recordLegResult(final RawResult raw_result) {

        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult) overall_results.get(team_index);

        final int leg_index = findIndexOfNextUnfilledLegResult(result.getLegResults());
        final RelayRaceLegResult leg_result = result.getLegResult(leg_index + 1);

        leg_result.setFinishTime(raw_result.getRecordedFinishTime());

        // Leg number will be unknown in most cases, unless explicitly recorded in raw results.
        leg_result.setLegNumber(((RelayRace) race).getExplicitlyRecordedLegNumbers().getOrDefault(raw_result, UNKNOWN_LEG_NUMBER));

        // Provisionally this leg is not DNF since a finish time was recorded.
        // However, it might still be set to DNF in recordDNF() if the runner missed a checkpoint.
        leg_result.setDnf(false);
    }

    private void sortLegResults() {

        overall_results.forEach(this::sortLegResults);
    }

    private void sortLegResults(final RaceResult result) {

        final List<RelayRaceLegResult> leg_results = ((RelayRaceResult) result).getLegResults();

        // Sort by explicitly recorded leg number (most results will not have explicit leg number).
        leg_results.sort(comparingInt(RelayRaceLegResult::getLegNumber));

        // Reset the leg numbers according to new positions in leg sequence.
        for (int leg_index = 1; leg_index <= leg_results.size(); leg_index++)
            leg_results.get(leg_index - 1).setLegNumber(leg_index);
    }

    private RelayRaceLegResult getLegResult(final int bib_number, final int leg_number) {

        final RelayRaceResult result = (RelayRaceResult) overall_results.get(findIndexOfTeamWithBibNumber(bib_number));

        return result.getLegResult(leg_number);
    }

    private void fillLegResultDetails() {

        overall_results.forEach(this::fillLegResultDetails);
    }

    private void fillLegResultDetails(final RaceResult result) {

        final List<RelayRaceLegResult> leg_results = ((RelayRaceResult) result).getLegResults();

        for (int leg_index = 0; leg_index < ((RelayRace) race).getNumberOfLegs(); leg_index++)
            fillLegResultDetails(leg_results, leg_index);
    }

    private void fillLegResultDetails(final List<? extends RelayRaceLegResult> leg_results, final int leg_index) {

        final RelayRaceLegResult leg_result = leg_results.get(leg_index);

        final Duration individual_start_time = getIndividualStartTime(leg_result, leg_index);
        final Duration leg_mass_start_time = ((RelayRace) race).getStartTimesForMassStarts().get(leg_index);
        final Duration previous_team_member_finish_time = leg_index > 0 ? leg_results.get(leg_index - 1).getFinishTime() : null;

        final Duration start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);
        final boolean in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);

        leg_result.setStartTime(start_time);
        leg_result.setInMassStart(in_mass_start);
    }

    private Duration getIndividualStartTime(final RelayRaceLegResult leg_result, final int leg_index) {

        return ((RelayRace) race).getIndividualStarts().stream().
            filter(individual_leg_start -> individual_leg_start.bib_number() == leg_result.getBibNumber()).
            filter(individual_leg_start -> individual_leg_start.leg_number() == leg_index + 1).
            map(RelayRace.IndividualStart::start_time).
            findFirst().
            orElse(null);
    }

    private Duration getLegStartTime(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_team_member_finish_time, final int leg_index) {

        Duration start_time;

        //////////////////////////////////////////////////////////////////////////////////////////////////

        // Check whether individual leg start time is recorded for this runner.
        if (individual_start_time != null) start_time = individual_start_time;

        // If there's no individual leg start time recorded (previous check), and this is a first leg runner, start at time zero.
        else if (leg_index == 0) start_time = Duration.ZERO;

        // This is a later leg runner (previous check). If there's no finish time recorded for previous runner, we can't
        // deduce a start time for this one. This leg result will be set to DNF by default.
        else if (previous_team_member_finish_time == null) start_time =  null;

        // There is a finish time for the previous runner (previous check), so use the earlier of the mass start time,
        // if present, and the previous runner's finish time.
        else start_time = mass_start_time != null && mass_start_time.compareTo(previous_team_member_finish_time) < 0 ?
            mass_start_time :
            previous_team_member_finish_time;

        //////////////////////////////////////////////////////////////////////////////////////////////////

        // Adjust start time for first leg runner if timing didn't start at zero.
        if (leg_index == 0) {

            // Get offset between actual race start time, and the time at which timing started.
            // Usually this is zero. A positive value indicates that the race started after timing started.
            final Duration race_start_time = (Duration) race.getConfig().get(KEY_RACE_START_TIME);

            start_time = start_time.plus(race_start_time);
        }

        return start_time;
    }

    private boolean isInMassStart(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // In mass start if it's not the first leg,  there is no individually recorded start time, and the previous
        // runner did not finish by the time of the mass start.

        final boolean individual_start_time_is_set = individual_start_time != null;
        final boolean mass_start_time_is_set = mass_start_time != null;
        final boolean previous_runner_finish_time_is_set = previous_runner_finish_time != null;

        final boolean previous_runner_not_finished_by_mass_start = mass_start_time_is_set &&
            (!previous_runner_finish_time_is_set || mass_start_time.compareTo(previous_runner_finish_time) < 0);
        final boolean first_leg = leg_index == 0;

        return !first_leg && !individual_start_time_is_set && previous_runner_not_finished_by_mass_start;
    }

    private static int findIndexOfNextUnfilledLegResult(final List<? extends RelayRaceLegResult> leg_results) {

        return (int) leg_results.stream().
            takeWhile(result -> result.getFinishTime() != null).
            count();
    }

    private int findIndexOfTeamWithBibNumber(final int bib_number) {

        return (int) overall_results.stream().
            map(result -> (RelayRaceResult)result).
            takeWhile(result -> result.getBibNumber() != bib_number).
            count();
    }

    private void addPaperRecordingComments() {

        final List<RawResult> raw_results = ((SingleRaceInternal) race).getRawResults();
        final int number_of_electronically_recorded_results = ((RelayRace) race).getNumberOfElectronicallyRecordedRawResults();

        if (number_of_electronically_recorded_results > 0 && number_of_electronically_recorded_results < raw_results.size())
            raw_results.get(number_of_electronically_recorded_results - 1).appendComment("Remaining times from paper recording sheet only.");
    }

    private RaceResult makeRaceResult(final RawResult raw_result) {

        final RaceEntry entry = getEntryWithBibNumber(raw_result.getBibNumber());
        return new RelayRaceResult(race, entry, null);
    }

    private RaceEntry getEntryWithBibNumber(final int bib_number) {

        return ((SingleRaceInternal) race).getEntries().stream().
            filter(entry -> entry.getBibNumber() == bib_number).
            findFirst().
            orElseThrow();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void interpolateMissingTimes() {

        final int index_of_first_result_with_recorded_time = getIndexOfFirstResultWithRecordedTime();

        // Before the first recorded time, all results get the first recorded time.
        setTimesForResultsBeforeFirstRecordedTime(index_of_first_result_with_recorded_time);

        setTimesForResultsAfterFirstRecordedTime(index_of_first_result_with_recorded_time);
    }

    private void guessMissingBibNumbers() {

        // Missing bib numbers are only guessed if a full set of finish times has been recorded,
        // i.e. all runner_names have finished.

        if (areTimesRecordedForAllRunners())
            guessMissingBibNumbersWithAllTimesRecorded();
        else
            recordCommentsForNonGuessedResults();
    }

    private boolean areTimesRecordedForAllRunners() {

        final RelayRace relay_race = (RelayRace) race;

        final int number_of_times_recorded = relay_race.getRawResults().size();
        final int number_of_runners = relay_race.getUniqueBibNumbersRecorded().size() * relay_race.getNumberOfLegs();

        return number_of_times_recorded == number_of_runners;
    }

    private int getIndexOfFirstResultWithRecordedTime() {

        return (int) ((RelayRace) race).getRawResults().stream().
            takeWhile(result -> result.getRecordedFinishTime() == null).
            count();
    }

    private void setTimesForResultsBeforeFirstRecordedTime(final int index_of_first_result_with_recorded_time) {

        final List<RawResult> results = ((RelayRace) race).getRawResults();
        final Duration first_recorded_time = results.get(index_of_first_result_with_recorded_time).getRecordedFinishTime();

        results.stream().
            limit(index_of_first_result_with_recorded_time).
            forEachOrdered(result -> {

                result.setRecordedFinishTime(first_recorded_time);
                result.appendComment("Time not recorded. No basis for interpolation so set to first recorded time.");
            });
    }

    private void setTimesForResultsAfterFirstRecordedTime(final int index_of_first_result_with_recorded_time) {

        final int number_of_results = ((RelayRace) race).getRawResults().size();

        int i = index_of_first_result_with_recorded_time;

        while (i < number_of_results) {

            final ContiguousSequence sequence = getNextContiguousSequenceWithMissingTimes(i);
            interpolateTimesForContiguousSequence(sequence);

            i = sequence.end_index + 1;
        }
    }

    private ContiguousSequence getNextContiguousSequenceWithMissingTimes(final int search_start_index) {

        final List<RawResult> results = ((RelayRace) race).getRawResults();
        final int number_of_results = results.size();

        int i = search_start_index;

        while (i < number_of_results && results.get(i).getRecordedFinishTime() != null) i++;
        final int missing_times_start_index = i;

        while (i < number_of_results && results.get(i).getRecordedFinishTime() == null) i++;
        final int missing_times_end_index = i - 1;

        return new ContiguousSequence(missing_times_start_index, missing_times_end_index);
    }

    private void interpolateTimesForContiguousSequence(final ContiguousSequence sequence) {

        final List<RawResult> results = ((RelayRace) race).getRawResults();

        if (furtherResultsAfterSequence(sequence)) {

            final Duration start_time = results.get(sequence.start_index - 1).getRecordedFinishTime();
            final Duration end_time = results.get(sequence.end_index + 1).getRecordedFinishTime();

            final int number_of_steps = sequence.end_index - sequence.start_index + 2;
            final Duration time_step = end_time.minus(start_time).dividedBy(number_of_steps);

            interpolateTimes(sequence, time_step);

        } else
            recordNoteForResultsAfterLastRecordedTime(sequence.start_index);
    }

    private boolean furtherResultsAfterSequence(final ContiguousSequence sequence) {

        // If the sequence ends before the end of results, there must be a further time
        // recorded, given how the sequence was constructed.
        return sequence.end_index < ((RelayRace) race).getRawResults().size() - 1;
    }

    private void interpolateTimes(final ContiguousSequence sequence, final Duration time_step) {

        final List<RawResult> results = ((RelayRace) race).getRawResults();
        final Duration finish_time_before_missing_sequence = results.get(sequence.start_index - 1).getRecordedFinishTime();

        for (int i = 0; i <= sequence.end_index - sequence.start_index; i++) {

            final Duration interpolated_finish_time = finish_time_before_missing_sequence.plus(time_step.multipliedBy(i + 1));
            final Duration rounded_interpolated_finish_time = roundToIntegerSeconds(interpolated_finish_time);

            final RawResult interpolated_result = results.get(sequence.start_index + i);

            interpolated_result.setRecordedFinishTime(rounded_interpolated_finish_time);
            interpolated_result.appendComment("Time not recorded. Time interpolated.");
        }
    }

    private static Duration roundToIntegerSeconds(final Duration duration) {

        long seconds = duration.getSeconds();
        if (duration.getNano() > HALF_A_SECOND_IN_NANOSECONDS) seconds++;
        return Duration.ofSeconds(seconds);
    }

    private void recordNoteForResultsAfterLastRecordedTime(final int missing_times_start_index) {

        ((RelayRace) race).getRawResults().stream().
            skip(missing_times_start_index).
            forEachOrdered(result -> result.appendComment("Time not recorded. No basis for interpolation so set to last recorded time + 1s."));
    }

    public Duration getLastRecordedFinishTime() {

        return ((RelayRace) race).getRawResults().reversed().stream().
            map(RawResult::getRecordedFinishTime).
            filter(Objects::nonNull).
            findFirst().
            orElseThrow();
    }

    private void recordCommentsForNonGuessedResults() {

        ((RelayRace) race).getRawResults().stream().
            filter(result -> result.getBibNumber() == UNKNOWN_BIB_NUMBER).
            forEach(result -> result.appendComment("Time but not bib number recorded electronically. Bib number not recorded on paper. Too many missing times to guess from DNF teams."));
    }

    private void guessMissingBibNumbersWithAllTimesRecorded() {

        final List<RawResult> results = ((RelayRace) race).getRawResults();

        int position_of_missing_bib_number = getPositionOfNextMissingBibNumber();
        while (position_of_missing_bib_number > 0) {

            final RawResult result_with_missing_number = results.get(position_of_missing_bib_number - 1);
            final int guessed_number = guessTeamNumber(position_of_missing_bib_number);

            result_with_missing_number.setBibNumber(guessed_number);
            result_with_missing_number.appendComment("Time but not bib number recorded electronically. Bib number not recorded on paper. Guessed bib number from DNF teams.");

            position_of_missing_bib_number = getPositionOfNextMissingBibNumber();
        }
    }

    private int getPositionOfNextMissingBibNumber() {

        final List<RawResult> results = ((RelayRace) race).getRawResults();

        final int index = (int) results.stream().
            takeWhile(result -> result.getBibNumber() != UNKNOWN_BIB_NUMBER).
            count();

        return index < results.size() ? index + 1 : 0;
    }

    private int guessTeamNumber(final int position) {

        // The general assumption here is that most teams have roughly similar performance,
        // so if one team has fewer finishes than the others at this point, we guess
        // that it's the one finishing now.

        // Get summary of each team's state at the point of this position being recorded,
        // in terms of how many of the team's runner_names finished before and after this position,
        // and the team's previous and next finish times.
        final List<TeamSummaryAtPosition> summaries = summarise(position);

        // Sort the summaries by: number of previous finishes, then number of subsequent
        // finishes, then time of subsequent finish, then time of previous finish.
        sort(summaries);

        // Guess the team with the fewest previous finishes, using the other attributes
        // described above as tie-breaks.
        return summaries.getFirst().team_number;
    }

    private List<TeamSummaryAtPosition> summarise(final int position) {

        return makeMutableCopy(
            ((RelayRace) race).getUniqueBibNumbersRecorded().stream().
                map(bib_number -> summarise(position, bib_number)).
                toList());
    }

    private TeamSummaryAtPosition summarise(final int position, final int bib_number) {

        final int finishes_before = getNumberOfTeamFinishesBetween(0, position - 1, bib_number);
        final int finishes_after = getNumberOfTeamFinishesBetween(position, ((RelayRace) race).getRawResults().size(), bib_number);

        final Duration previous_finish_time = getPreviousTeamFinishTime(position, bib_number);
        final Duration next_finish_time = getNextTeamFinishTime(position, bib_number);

        return new TeamSummaryAtPosition(bib_number, finishes_before, finishes_after, previous_finish_time, next_finish_time);
    }

    private int getNumberOfTeamFinishesBetween(final int position1, final int position2, final int bib_number) {

        return (int) ((RelayRace) race).getRawResults().stream().
            limit(position2).
            skip(position1).
            filter(result -> result.getBibNumber() == bib_number).
            count();
    }

    private Duration getPreviousTeamFinishTime(final int position, final int bib_number) {

        // Subtract two since looking at previous result and converting from position to index.
        final Stream<RawResult> stream = ((RelayRace) race).getRawResults().reversed().stream().limit(position - 2);

        return getFinishTimeForMatchingBibNumber(bib_number, stream);
    }

    private Duration getNextTeamFinishTime(final int position, final int bib_number) {

        final Stream<RawResult> stream = ((RelayRace) race).getRawResults().stream().skip(position);

        return getFinishTimeForMatchingBibNumber(bib_number, stream);
    }

    private static Duration getFinishTimeForMatchingBibNumber(final int bib_number, final Stream<RawResult> stream) {

        return stream.
            filter(result -> result.getBibNumber() == bib_number).
            map(RawResult::getRecordedFinishTime).
            findFirst().
            orElse(Duration.ZERO);
    }

    private static void sort(final List<TeamSummaryAtPosition> summaries) {

        summaries.sort(getTeamSummaryComparator());
    }

    private static Comparator<TeamSummaryAtPosition> getTeamSummaryComparator() {

        return team_summary_comparators.stream().
            reduce((_, _) -> 0, Comparator::thenComparing);
    }
}
