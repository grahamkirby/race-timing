package lap_race;

import common.Race;
import common.RawResult;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;

public class LapRaceMissingData {

    private record TeamSummaryAtPosition(int team_number, int finishes_before, int finishes_after, Duration previous_finish, Duration next_finish) { }

    private final LapRace race;

    public LapRaceMissingData(LapRace race) {
        this.race = race;
    }

    public void interpolateMissingTimes() {

        final int index_of_first_result_with_recorded_time = getIndexOfFirstResultWithRecordedTime();

        // Results before the first recorded time get the first recorded time.
        setTimesForResultsBeforeFirstRecordedTime(index_of_first_result_with_recorded_time);

        setTimesForResultsAfterFirstRecordedTime(index_of_first_result_with_recorded_time);
    }

    public void guessMissingBibNumbers() {

        // Missing bib numbers are only guessed if a full set of finish times has been recorded,
        // i.e. all runners have finished.

        if (race.getRawResults().length != race.entries.length * race.number_of_legs)
            recordCommentsForNonGuessedResults();
        else
            guessMissingBibNumbersWithAllTimesRecorded();
    }

    private int getIndexOfFirstResultWithRecordedTime() {

        int raw_result_index = 0;
        while (raw_result_index < race.getRawResults().length && race.getRawResults()[raw_result_index].getRecordedFinishTime() == null) raw_result_index++;
        return raw_result_index;
    }

    private void setTimesForResultsBeforeFirstRecordedTime(final int index_of_first_result_with_recorded_time) {

        final Duration first_recorded_time = race.getRawResults()[index_of_first_result_with_recorded_time].getRecordedFinishTime();

        for (int i = 0; i < index_of_first_result_with_recorded_time; i++) {

            race.getRawResults()[i].setRecordedFinishTime(first_recorded_time);
            race.getRawResults()[i].appendComment("Time not recorded. No basis for interpolation so set to first recorded time.");
        }
    }

    private void setTimesForResultsAfterFirstRecordedTime(final int index_of_first_result_with_recorded_time) {

        int i = index_of_first_result_with_recorded_time;

        while (i < race.getRawResults().length) {

            while (i < race.getRawResults().length && race.getRawResults()[i].getRecordedFinishTime() != null) i++;
            final int missing_times_start_index = i - 1;

            while (i < race.getRawResults().length && race.getRawResults()[i].getRecordedFinishTime() == null) i++;
            final int missing_times_end_index = i;

            setTimesForResultsAfterFirstRecordedTime(missing_times_start_index, missing_times_end_index);
        }
    }

    private void setTimesForResultsAfterFirstRecordedTime(final int missing_times_start_index, final int missing_times_end_index) {

        if (missing_times_end_index < race.getRawResults().length)
            interpolateTimes(missing_times_start_index, missing_times_end_index);

        else
            // Results after the last recorded time get the last recorded time.
            setTimesForResultsAfterLastRecordedTime(missing_times_start_index);
    }

    private void interpolateTimes(final int missing_times_start_index, final int missing_times_end_index) {

        final Duration start_time = race.getRawResults()[missing_times_start_index].getRecordedFinishTime();
        final Duration end_time = race.getRawResults()[missing_times_end_index].getRecordedFinishTime();

        final int number_of_steps = missing_times_end_index - missing_times_start_index;
        final Duration time_step = end_time.minus(start_time).dividedBy(number_of_steps);

        interpolateTimes(missing_times_start_index, missing_times_end_index, time_step);
    }

    private void interpolateTimes(final int missing_times_start_index, final int missing_times_end_index, final Duration time_step) {

        for (int i = missing_times_start_index + 1; i < missing_times_end_index; i++) {

            final Duration previous_finish_time = race.getRawResults()[i - 1].getRecordedFinishTime();

            race.getRawResults()[i].setRecordedFinishTime(previous_finish_time.plus(time_step));
            race.getRawResults()[i].appendComment("Time not recorded. Time interpolated.");
        }
    }

    private void setTimesForResultsAfterLastRecordedTime(final int missing_times_start_index) {

        final Duration last_recorded_time = race.getRawResults()[missing_times_start_index].getRecordedFinishTime();

        for (int i = missing_times_start_index + 1; i < race.getRawResults().length; i++) {

            race.getRawResults()[i].setRecordedFinishTime(last_recorded_time);
            race.getRawResults()[i].appendComment("Time not recorded. No basis for interpolation so set to last recorded time.");
        }
    }

    private void recordCommentsForNonGuessedResults() {

        for (final RawResult raw_result : race.getRawResults())
            if (raw_result.getBibNumber() == null)
                raw_result.appendComment("Time but not bib number recorded electronically. Bib number not recorded on paper. Too many missing times to guess from DNF teams.");
    }

    private void guessMissingBibNumbersWithAllTimesRecorded() {

        int position_of_missing_bib_number = getPositionOfNextMissingBibNumber();
        while (position_of_missing_bib_number > 0) {

            final RawResult result_with_missing_number = race.getRawResults()[position_of_missing_bib_number - 1];
            final int guessed_number = guessTeamNumber(position_of_missing_bib_number);

            result_with_missing_number.setBibNumber(guessed_number);
            result_with_missing_number.appendComment("Time but not bib number recorded electronically. Bib number not recorded on paper. Guessed bib number from DNF teams.");

            position_of_missing_bib_number = getPositionOfNextMissingBibNumber();
        }
    }

    private int getPositionOfNextMissingBibNumber() {

        for (int i = 0; i < race.getRawResults().length; i++)
            if (race.getRawResults()[i].getBibNumber() == null) return i + 1;

        return 0;
    }

    private int guessTeamNumber(final int position) {

        // The general assumption here is that most teams have roughly similar performance,
        // so if one team has fewer finishes than the others at this point, we guess
        // that it's the one finishing now.

        // Get summary of each team's state at the point of this position being recorded,
        // in terms of how many of the team's runners finished before and after this position,
        // and the team's previous and next finish times.
        final TeamSummaryAtPosition[] summaries = summarise(position);

        // Sort the summaries by: number of previous finishes, then number of subsequent
        // finishes, then time of subsequent finish, then time of previous finish.
        sort(summaries);

        // Guess the team with the fewest previous finishes, using the other attributes
        // described above as tie-breaks.
        return summaries[0].team_number;
    }

    private TeamSummaryAtPosition[] summarise(final int position) {

        final TeamSummaryAtPosition[] summaries = new TeamSummaryAtPosition[race.entries.length];

        for (int i = 0; i < summaries.length; i++)
            summaries[i] = summarise(position, race.entries[i].bib_number);

        return summaries;
    }

    private TeamSummaryAtPosition summarise(final int position, final int bib_number) {

        final int finishes_before = getNumberOfTeamFinishesBefore(position, bib_number);
        final int finishes_after = getNumberOfTeamFinishesAfter(position, bib_number);

        final Duration previous_finish_time = getPreviousTeamFinishTime(position, bib_number);
        final Duration next_finish_time = getNextTeamFinishTime(position, bib_number);

        return new TeamSummaryAtPosition(bib_number, finishes_before, finishes_after, previous_finish_time, next_finish_time);
    }

    private void sort(final TeamSummaryAtPosition[] summaries) {

        Arrays.sort(summaries, Comparator.comparing(o -> o.previous_finish));
        Arrays.sort(summaries, Comparator.comparing(o -> o.next_finish));
        Arrays.sort(summaries, Comparator.comparingInt(o -> o.finishes_after));
        Arrays.sort(summaries, Comparator.comparingInt(o -> o.finishes_before));
    }

    private int getNumberOfTeamFinishesBefore(final int starting_index, final int bib_number) {

        int count = 0;
        for (int i = starting_index - 1; i > 0; i--) {

            final Integer result_bib_number = race.getRawResults()[i - 1].getBibNumber();
            if (result_bib_number != null && result_bib_number == bib_number) count++;
        }

        return count;
    }

    private int getNumberOfTeamFinishesAfter(final int starting_index, final int bib_number) {

        int count = 0;
        for (int i = starting_index + 1; i <= race.getRawResults().length; i++) {

            final Integer result_bib_number = race.getRawResults()[i - 1].getBibNumber();
            if (result_bib_number != null && result_bib_number == bib_number) count++;
        }

        return count;
    }

    private Duration getPreviousTeamFinishTime(final int starting_index, final int bib_number) {

        for (int i = starting_index - 1; i > 0; i--) {

            final RawResult result = race.getRawResults()[i - 1];
            final Integer result_bib_number = result.getBibNumber();

            if (result_bib_number != null && result_bib_number == bib_number) return result.getRecordedFinishTime();
        }

        return Race.ZERO_TIME;
    }

    private Duration getNextTeamFinishTime(final int starting_index, final int bib_number) {

        for (int i = starting_index + 1; i <= race.getRawResults().length; i++) {

            final RawResult result = race.getRawResults()[i - 1];
            final Integer result_bib_number = result.getBibNumber();

            if (result_bib_number != null && result_bib_number == bib_number) return result.getRecordedFinishTime();
        }

        return Race.ZERO_TIME;
    }
}
