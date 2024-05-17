package minitour;

import common.Race;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import individual_race.Runner;
import lap_race.LapRace;
import lap_race.LegResult;

import java.time.Duration;
import java.util.Arrays;

public class MinitourRaceResult implements Comparable<MinitourRaceResult> {

    final Runner runner;

    final Duration[] times;
    final MinitourRace race;
    String position_string;

    public MinitourRaceResult(final Runner runner, final MinitourRace race) {

        this.runner = runner;
        this.race = race;

        times = new Duration[race.races.length];
    }

    public Duration duration() {

        Duration overall = Duration.ZERO;

        for (final Duration time : times) {

            if (time != null)
                overall = overall.plus(time);
        }

        return overall;
    }

    public boolean completed() {

        return numberCompleted() == race.races.length;
    }

    private int numberCompleted() {

        int count = 0;

        for (final IndividualRace individual_race : race.races) {
            if (individual_race != null)
                for (final IndividualRaceResult result : individual_race.getOverallResults())
                    if (result.entry.runner.equals(runner)) count++;
        }

        return count;
    }

    public boolean completedAllRacesSoFar() {

        for (int i = 0; i < race.races.length; i++)
            if (race.races[i] != null && times[i] == null)
                return false;

        return true;
    }

    @Override
    public int compareTo(final MinitourRaceResult o) {

        if (completed() && !o.completed()) return -1;

        if (!completed() && o.completed()) return 1;

        if (duration().compareTo(o.duration()) < 0) return -1;

        if (duration().compareTo(o.duration()) > 0) return 1;

        int last_name_comparison = Race.getLastName(runner.name).compareTo(Race.getLastName(o.runner.name));

        return last_name_comparison != 0 ? last_name_comparison : Race.getFirstName(runner.name).compareTo(Race.getFirstName(o.runner.name));
    }
}
