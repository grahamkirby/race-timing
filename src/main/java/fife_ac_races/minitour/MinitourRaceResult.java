package fife_ac_races.minitour;

import common.RaceResult;
import common.Runner;
import individual_race.IndividualRace;
import series_race.SeriesRaceResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MinitourRaceResult extends SeriesRaceResult {

    public final List<Duration> times;

    public MinitourRaceResult(final Runner runner, final MinitourRace race) {

        super(runner, race);

        times = new ArrayList<>();
//        for (int i = 0; i < race.races.size(); i++)
//            times.add(null);
    }

    protected Duration duration() {

        Duration overall = Duration.ZERO;

        for (final Duration time : times)
            if (time != null)
                overall = overall.plus(time);

        return overall;
    }

    public boolean raceHasTakenPlace(int race_number) {

        return ((MinitourRace)race).getRaces().get(race_number - 1) != null;
    }

    @Override
    public boolean completedAllRacesSoFar() {

        List<IndividualRace> races = ((MinitourRace)race).getRaces();

        for (int i = 0; i < races.size(); i++)
            if (races.get(i) != null && times.get(i) == null)
                return false;

        return true;
    }

    @Override
    public int compareTo(final RaceResult other) {

        return compare(this, other);

    }

    @Override
    public int comparePerformanceTo(RaceResult other) {

        return duration().compareTo(((MinitourRaceResult) other).duration());
    }

    public static int compare(final RaceResult r1, final RaceResult r2) {
        
        final int compare_completion = ((MinitourRaceResult) r1).compareCompletionTo((MinitourRaceResult) r2);
        if (compare_completion != 0) return compare_completion;

        final int compare_completion_so_far = ((MinitourRaceResult) r1).compareCompletionSoFarTo((MinitourRaceResult) r2);
        if (compare_completion_so_far != 0) return compare_completion_so_far;

        if (((MinitourRaceResult) r1).completedAllRacesSoFar()) {

            final int compare_performance = r1.comparePerformanceTo((MinitourRaceResult) r2);
            if (compare_performance != 0) return compare_performance;
        }

        return ((MinitourRaceResult) r1).compareRunnerNameTo((MinitourRaceResult) r2);
    }
}
