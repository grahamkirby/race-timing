/*
 * Copyright 2025 Graham Kirby:
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

import org.grahamkirby.race_timing.common.RawResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;

class RelayRaceMissingData {

    private record TeamSummaryAtPosition(int team_number, int finishes_before, int finishes_after,
                                         Duration previous_finish, Duration next_finish) {
    }

    private record ContiguousSequence(int start_index, int end_index) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int HALF_A_SECOND_IN_NANOSECONDS = 500000000;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final RelayRace race;

    RelayRaceMissingData(final RelayRace race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void interpolateMissingTimes() {

        final int index_of_first_result_with_recorded_time = getIndexOfFirstResultWithRecordedTime();

        // Results before the first recorded time get the first recorded time.
        setTimesForResultsBeforeFirstRecordedTime(index_of_first_result_with_recorded_time);

        setTimesForResultsAfterFirstRecordedTime(index_of_first_result_with_recorded_time);
    }

    void guessMissingBibNumbers() {

        // Missing bib numbers are only guessed if a full set of finish times has been recorded,
        // i.e. all runner_names have finished.

        if (areTimesRecordedForAllRunners())
            guessMissingBibNumbersWithAllTimesRecorded();
        else
            recordCommentsForNonGuessedResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean areTimesRecordedForAllRunners() {

        return race.getRawResults().size() == race.entries.size() * race.getNumberOfLegs();
    }

    private int getIndexOfFirstResultWithRecordedTime() {

        int raw_result_index = 0;
        while (raw_result_index < race.getRawResults().size() && race.getRawResults().get(raw_result_index).getRecordedFinishTime() == null)
            raw_result_index++;
        return raw_result_index;
    }

    private void setTimesForResultsBeforeFirstRecordedTime(final int index_of_first_result_with_recorded_time) {

        final Duration first_recorded_time = race.getRawResults().get(index_of_first_result_with_recorded_time).getRecordedFinishTime();

        for (int i = 0; i < index_of_first_result_with_recorded_time; i++) {

            race.getRawResults().get(i).setRecordedFinishTime(first_recorded_time);
            race.getRawResults().get(i).appendComment("Time not recorded. No basis for interpolation so set to first recorded time.");
        }
    }

    private void setTimesForResultsAfterFirstRecordedTime(final int index_of_first_result_with_recorded_time) {

        int i = index_of_first_result_with_recorded_time;

        while (i < race.getRawResults().size()) {

            final ContiguousSequence sequence = getNextContiguousSequenceWithMissingTimes(i);
            interpolateTimesForContiguousSequence(sequence);

            i = sequence.end_index + 1;
        }
    }

    private ContiguousSequence getNextContiguousSequenceWithMissingTimes(final int search_start_index) {

        int i = search_start_index;

        while (i < race.getRawResults().size() && race.getRawResults().get(i).getRecordedFinishTime() != null) i++;
        final int missing_times_start_index = i;

        while (i < race.getRawResults().size() && race.getRawResults().get(i).getRecordedFinishTime() == null) i++;
        final int missing_times_end_index = i - 1;

        return new ContiguousSequence(missing_times_start_index, missing_times_end_index);
    }

    private void interpolateTimesForContiguousSequence(final ContiguousSequence sequence) {

        // For results after the last recorded time, use the last recorded time.
        if (isLastResult(sequence.end_index)) {
            setTimesForResultsAfterLastRecordedTime(sequence.start_index);

        } else {

            final Duration start_time = race.getRawResults().get(sequence.start_index - 1).getRecordedFinishTime();
            final Duration end_time = race.getRawResults().get(sequence.end_index + 1).getRecordedFinishTime();

            final int number_of_steps = sequence.end_index - sequence.start_index + 2;
            final Duration time_step = end_time.minus(start_time).dividedBy(number_of_steps);

            interpolateTimes(sequence, time_step);
        }
    }

    private boolean isLastResult(final int end_index) {
        return end_index == race.getRawResults().size() - 1;
    }

    private void interpolateTimes(final ContiguousSequence sequence, final Duration time_step) {

        final Duration finish_time_before_missing_sequence = race.getRawResults().get(sequence.start_index - 1).getRecordedFinishTime();

        for (int i = 0; i <= sequence.end_index - sequence.start_index; i++) {

            final Duration interpolated_finish_time = finish_time_before_missing_sequence.plus(time_step.multipliedBy(i + 1));
            final Duration rounded_interpolated_finish_time = roundToIntegerSeconds(interpolated_finish_time);

            final RawResult interpolated_result = race.getRawResults().get(sequence.start_index + i);

            interpolated_result.setRecordedFinishTime(rounded_interpolated_finish_time);
            interpolated_result.appendComment("Time not recorded. Time interpolated.");
        }
    }

    private static Duration roundToIntegerSeconds(final Duration duration) {

        long seconds = duration.getSeconds();
        if (duration.getNano() > HALF_A_SECOND_IN_NANOSECONDS) seconds++;
        return Duration.ofSeconds(seconds);
    }

    private void setTimesForResultsAfterLastRecordedTime(final int missing_times_start_index) {

        final Duration last_recorded_time = race.getRawResults().get(missing_times_start_index - 1).getRecordedFinishTime();

        for (int i = missing_times_start_index; i < race.getRawResults().size(); i++) {

            final RawResult missing_result = race.getRawResults().get(i);

            missing_result.setRecordedFinishTime(last_recorded_time);
            missing_result.appendComment("Time not recorded. No basis for interpolation so set to last recorded time.");
        }
    }

    private void recordCommentsForNonGuessedResults() {

        for (final RawResult result : race.getRawResults())
            if (result.getBibNumber() == UNKNOWN_BIB_NUMBER)
                result.appendComment("Time but not bib number recorded electronically. Bib number not recorded on paper. Too many missing times to guess from DNF teams.");
    }

    private void guessMissingBibNumbersWithAllTimesRecorded() {

        int position_of_missing_bib_number = getPositionOfNextMissingBibNumber();
        while (position_of_missing_bib_number > 0) {

            final RawResult result_with_missing_number = race.getRawResults().get(position_of_missing_bib_number - 1);
            final int guessed_number = guessTeamNumber(position_of_missing_bib_number);

            result_with_missing_number.setBibNumber(guessed_number);
            result_with_missing_number.appendComment("Time but not bib number recorded electronically. Bib number not recorded on paper. Guessed bib number from DNF teams.");

            position_of_missing_bib_number = getPositionOfNextMissingBibNumber();
        }
    }

    private int getPositionOfNextMissingBibNumber() {

        for (int i = 0; i < race.getRawResults().size(); i++)
            if (race.getRawResults().get(i).getBibNumber() == UNKNOWN_BIB_NUMBER) return i + 1;

        return 0;
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

        return new ArrayList<>(race.entries.stream().map(entry -> summarise(position, entry.bib_number)).toList());
    }

    private TeamSummaryAtPosition summarise(final int position, final int bib_number) {

        final int finishes_before = getNumberOfTeamFinishesBetween(0, position - 1, bib_number);
        final int finishes_after = getNumberOfTeamFinishesBetween(position, race.getRawResults().size(), bib_number);

        final Duration previous_finish_time = getPreviousTeamFinishTime(position, bib_number);
        final Duration next_finish_time = getNextTeamFinishTime(position, bib_number);

        return new TeamSummaryAtPosition(bib_number, finishes_before, finishes_after, previous_finish_time, next_finish_time);
    }

    private int getNumberOfTeamFinishesBetween(final int position1, final int position2, final int bib_number) {

        int count = 0;
        for (int i = position1; i < position2; i++)
            if (race.getRawResults().get(i).getBibNumber() == bib_number) count++;

        return count;
    }

    private Duration getPreviousTeamFinishTime(final int position, final int bib_number) {

        for (int i = position - 1; i > 0; i--) {

            final Duration finish_time = getFinishTimeIfBibNumberMatches(bib_number, i);
            if (finish_time != null) return finish_time;
        }

        return Duration.ZERO;
    }

    private Duration getNextTeamFinishTime(final int position, final int bib_number) {

        for (int i = position + 1; i <= race.getRawResults().size(); i++) {

            final Duration finish_time = getFinishTimeIfBibNumberMatches(bib_number, i);
            if (finish_time != null) return finish_time;
        }

        return Duration.ZERO;
    }

    private Duration getFinishTimeIfBibNumberMatches(final int bib_number, final int position) {

        final RawResult result = race.getRawResults().get(position - 1);
        final int result_bib_number = result.getBibNumber();

        return (result_bib_number == bib_number) ? result.getRecordedFinishTime() : null;
    }

    private static void sort(final List<TeamSummaryAtPosition> summaries) {

        summaries.sort(Comparator.comparing(o -> o.previous_finish));
        summaries.sort(Comparator.comparing(o -> o.next_finish));
        summaries.sort(Comparator.comparingInt(o -> o.finishes_after));
        summaries.sort(Comparator.comparingInt(o -> o.finishes_before));
    }
}
