package series_race;

import common.Race;
import common.RaceResult;
import common.Runner;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;

public abstract class SeriesRaceResult extends RaceResult {

    public final Runner runner;

    public SeriesRaceResult(final Runner runner, final SeriesRace race) {

        super(race);
        this.runner = runner;
    }

    public boolean completed() {

        return numberCompleted() >= ((SeriesRace)race).minimum_number_of_races;
    }

    public abstract boolean completedAllRacesSoFar();

    protected int numberCompleted() {

        int count = 0;

        for (final IndividualRace individual_race : ((SeriesRace)race).races) {
            if (individual_race != null)
                for (final RaceResult result : individual_race.getOverallResults())
                    if (((IndividualRaceResult)result).entry.runner.equals(runner)) count++;
        }

        return count;
    }

    protected int compareCompletionTo(SeriesRaceResult o) {

        if (completed() && !o.completed()) return -1;
        if (!completed() && o.completed()) return 1;
        return 0;
    }

    protected int compareCompletionSoFarTo(SeriesRaceResult o) {

        if (completedAllRacesSoFar() && !o.completedAllRacesSoFar()) return -1;
        if (!completedAllRacesSoFar() && o.completedAllRacesSoFar()) return 1;
        return 0;
    }

    protected int compareRunnerNameTo(SeriesRaceResult o) {

        final int last_name_comparison = Race.getLastName(runner.name).compareTo(Race.getLastName(o.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : Race.getFirstName(runner.name).compareTo(Race.getFirstName(o.runner.name));
    }
}
