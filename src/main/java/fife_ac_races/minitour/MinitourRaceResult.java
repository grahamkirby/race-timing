package fife_ac_races.minitour;

import common.Runner;
import series_race.SeriesRaceResult;

import java.time.Duration;

public class MinitourRaceResult extends SeriesRaceResult {

    public final Duration[] times;

    public MinitourRaceResult(final Runner runner, final MinitourRace race) {

        super(runner, race);
        times = new Duration[race.races.length];
    }

    protected Duration duration() {

        Duration overall = Duration.ZERO;

        for (final Duration time : times) {

            if (time != null)
                overall = overall.plus(time);
        }

        return overall;
    }

    public boolean raceHasTakenPlace(int race_number) {

        return race.races[race_number - 1] != null;
    }

    public boolean completedAllRacesSoFar() {

        for (int i = 0; i < race.races.length; i++)
            if (race.races[i] != null && times[i] == null)
                return false;

        return true;
    }

    @Override
    public int comparePerformanceTo(SeriesRaceResult o) {

        return duration().compareTo(((MinitourRaceResult)o).duration());
    }

    @Override
    public int compareTo(final SeriesRaceResult o) {

        final int compare_completion = compareCompletionTo(o);
        if (compare_completion != 0) return compare_completion;

        final int compare_completion_so_far = compareCompletionSoFarTo(o);
        if (compare_completion_so_far != 0) return compare_completion_so_far;

        if (completedAllRacesSoFar()) {

            final int compare_performance = comparePerformanceTo(o);
            if (compare_performance != 0) return compare_performance;
        }

        return compareRunnerNameTo(o);
    }
}
