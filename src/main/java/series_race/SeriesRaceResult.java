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

        if (completed() && !o.completed()) return -1;

        if (!completed() && o.completed()) return 1;

        if (totalScore() > o.totalScore()) return -1;

        if (totalScore() < o.totalScore()) return 1;

        return getSurname(runner.name()).compareTo(getSurname(o.runner.name()));
    }

    private String getSurname(String name) {

        return name.split(" ")[1];
    }
}
