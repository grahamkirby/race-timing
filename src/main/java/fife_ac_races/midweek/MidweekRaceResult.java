package fife_ac_races.midweek;

import common.Race;
import common.Runner;
import fife_ac_races.minitour.MinitourRaceResult;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import series_race.SeriesRaceResult;

import java.util.Arrays;

public class MidweekRaceResult extends SeriesRaceResult {

    public final int[] scores;
    public String position_string;

    public MidweekRaceResult(final Runner runner, final MidweekRace race) {

        super(runner, race);

        scores = new int[race.races.length];
        Arrays.fill(scores, -1);
    }

    public int totalScore() {

        int total = 0;

        int[] sorted_scores = scores.clone();
        Arrays.sort(sorted_scores);
        for (int i = 0; i < race.minimum_number_of_races; i++) {
            int score = sorted_scores[sorted_scores.length - 1 - i];
            if (score > -1) total += score;
        }

        return total;
    }

    @Override
    public boolean completedAllRacesSoFar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int comparePerformanceTo(SeriesRaceResult o) {
        return -Integer.compare(totalScore(), ((MidweekRaceResult) o).totalScore());
    }

    @Override
    public int compareTo(final SeriesRaceResult o) {

        int compare_completion = compareCompletion(o);
        if (compare_completion != 0) return compare_completion;

        int compare_performance = comparePerformanceTo(o);
        if (compare_performance != 0) return compare_performance;

        return compareRunnerName(o);
    }
}
