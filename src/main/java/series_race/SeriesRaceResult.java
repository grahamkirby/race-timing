package series_race;

import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import individual_race.Runner;

import java.util.Arrays;

public class SeriesRaceResult implements Comparable<SeriesRaceResult> {

    final Runner runner;

    final int[] scores;
    final SeriesRace race;
    String position_string;

    public SeriesRaceResult(final Runner runner, final SeriesRace race) {

        this.runner = runner;
        this.race = race;

        scores = new int[race.races.length];
        Arrays.fill(scores, -1);
    }

    public int totalScore() {

        int total = 0;
        for (int score : scores)
            if (score > -1)
                total += score;

        return total;
    }

    public boolean completed() {

        return numberCompleted() >= race.minimum_number_of_races;
    }

    private int numberCompleted() {

        int count = 0;

        for (IndividualRace individual_race : race.races) {
            if (individual_race != null)
                for (IndividualRaceResult result : individual_race.getOverallResults()) {
                    if (result.entry.runner.equals(runner)) count++;
                }
        }

        return count;
    }

    private int numberCompleted2() {

        int count = 0;
        for (int score : scores)
            if (score > -1)
                count++;

        return count;
    }

    @Override
    public int compareTo(final SeriesRaceResult o) {

        if (completed() && !o.completed()) return -1;

        if (!completed() && o.completed()) return 1;

        if (totalScore() > o.totalScore()) return -1;

        if (totalScore() < o.totalScore()) return 1;

        int last_name_comparison = getLastName(runner.name).compareTo(getLastName(o.runner.name));

        return last_name_comparison != 0 ? last_name_comparison : getFirstName(runner.name).compareTo(getFirstName(o.runner.name));
    }

    private String getFirstName(final String name) {
        final String[] names = name.split(" ");
        return names[0];
    }

    private String getLastName(final String name) {
        final String[] names = name.split(" ");
        return names[names.length - 1];
    }
}
