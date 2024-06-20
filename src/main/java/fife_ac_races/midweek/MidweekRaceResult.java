package fife_ac_races.midweek;

import common.RaceResult;
import common.Runner;
import series_race.SeriesRaceResult;

import java.util.ArrayList;
import java.util.List;

public class MidweekRaceResult extends SeriesRaceResult {

    public final List<Integer> scores;

    public MidweekRaceResult(final Runner runner, final MidweekRace race) {

        super(runner, race);

        scores = new ArrayList<>();
    }

    protected int totalScore() {

        int total = 0;

        final List<Integer> sorted_scores = new ArrayList<>(scores);
        sorted_scores.sort(Integer::compareTo);

        for (int i = 0; i < ((MidweekRace)race).getMinimumNumberOfRaces(); i++) {
            final int score = sorted_scores.get(sorted_scores.size() - 1 - i);
            if (score > -1) total += score;
        }

        return total;
    }

    @Override
    public boolean completedAllRacesSoFar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int comparePerformanceTo(RaceResult o) {

        // Negate so that a higher score gives an earlier ranking.
        return -Integer.compare(totalScore(), ((MidweekRaceResult) o).totalScore());
    }

    @Override
    public int compareTo(final RaceResult other) {
        return compare(this, other);
    }

    public static int compare(final RaceResult r1, final RaceResult r2) {

        final int compare_completion = ((MidweekRaceResult)r1).compareCompletionTo((MidweekRaceResult) r2);
        if (compare_completion != 0) return compare_completion;

        final int compare_performance = r1.comparePerformanceTo((MidweekRaceResult) r2);
        if (compare_performance != 0) return compare_performance;

        return ((MidweekRaceResult)r1).compareRunnerNameTo((MidweekRaceResult) r2);
    }
}
