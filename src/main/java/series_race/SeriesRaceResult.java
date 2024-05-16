package series_race;

import common.Race;
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

        int[] sorted_scores = scores.clone();
        Arrays.sort(sorted_scores);
        for (int i = 0; i < race.minimum_number_of_races; i++) {
            int score = sorted_scores[sorted_scores.length - 1 - i];
            if (score > -1) total += score;
        }

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

    @Override
    public int compareTo(final SeriesRaceResult o) {

        if (completed() && !o.completed()) return -1;

        if (!completed() && o.completed()) return 1;

        if (totalScore() > o.totalScore()) return -1;

        if (totalScore() < o.totalScore()) return 1;

        int last_name_comparison = Race.getLastName(runner.name).compareTo(Race.getLastName(o.runner.name));

        return last_name_comparison != 0 ? last_name_comparison : Race.getFirstName(runner.name).compareTo(Race.getFirstName(o.runner.name));
    }
}
