package series_race;

import common.Race;
import common.Runner;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;

public abstract class SeriesRaceResult implements Comparable<SeriesRaceResult> {

    public final Runner runner;
    protected final SeriesRace race;

    public SeriesRaceResult(final Runner runner, final SeriesRace race) {

        this.runner = runner;
        this.race = race;
    }

    public boolean completed() {

        return numberCompleted() >= race.minimum_number_of_races;
    }

    public abstract boolean completedAllRacesSoFar();
    protected int numberCompleted() {

        int count = 0;

        for (final IndividualRace individual_race : race.races) {
            if (individual_race != null)
                for (final IndividualRaceResult result : individual_race.getOverallResults())
                    if (result.entry.runner.equals(runner)) count++;
        }

        return count;
    }

    protected int compareRunnerName(SeriesRaceResult o) {

        final int last_name_comparison = Race.getLastName(runner.name).compareTo(Race.getLastName(o.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : Race.getFirstName(runner.name).compareTo(Race.getFirstName(o.runner.name));
    }

    protected int compareCompletion(SeriesRaceResult o) {

        if (completed() && !o.completed()) return -1;
        if (!completed() && o.completed()) return 1;
        return 0;
    }

    protected abstract int comparePerformanceTo(SeriesRaceResult other);
}
