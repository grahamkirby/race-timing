package series_race;

import individual_race.IndividualRace;
import individual_race.IndividualRaceEntry;
import individual_race.Runner;

import java.time.Duration;
import java.util.Arrays;

import static common.Race.DUMMY_DURATION;

public class SeriesRaceResult implements Comparable<SeriesRaceResult> {

    final Runner runner;

    final int[] scores;
    final SeriesRace race;

    public SeriesRaceResult(final Runner runner, final SeriesRace race) {

        this.runner = runner;
        this.race = race;

        scores = new int[race.races.length];
        Arrays.fill(scores, -1);
    }

    private int totalScore() {

        int total = 0;
        for (int score : scores)
            if (score > -1)
                total += score;

        return total;
    }

    private int cappedScore() {

        return completed() ? totalScore() : Integer.MIN_VALUE;
    }

    private boolean completed() {

        return numberCompleted() >= race.minimum_number_of_races;
    }

    private int numberCompleted() {

        int count = 0;
        for (int score : scores)
            if (score > -1)
                count++;

        return count;
    }

    @Override
    public int compareTo(SeriesRaceResult o) {

        if (cappedScore() == o.cappedScore())
            return runner.name().compareTo(o.runner.name());

        return Integer.compare(cappedScore(), o.cappedScore());
    }
}
