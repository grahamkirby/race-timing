package series_race;

import common.RaceResult;
import common.Runner;
import common.categories.Category;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;

public abstract class SeriesRaceResult extends RaceResult {

    public final Runner runner;

    public SeriesRaceResult(final Runner runner, final SeriesRace race) {

        super(race);
        this.runner = runner;
    }

    @Override
    public boolean completed() {

        return numberCompleted() >= ((SeriesRace)race).minimum_number_of_races;
    }

    @Override
    public Category getCategory() {
        return runner.category;
    }

    public abstract boolean completedAllRacesSoFar();

    @Override
    public boolean sameEntrant(RaceResult other) {
        return runner.equals(((SeriesRaceResult) other).runner);
    }

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

        final int last_name_comparison = getLastName(runner.name).compareTo(getLastName(o.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : getFirstName(runner.name).compareTo(getFirstName(o.runner.name));
    }

    private static String getFirstName(final String name) {
        return name.split(" ")[0];
    }

    private static String getLastName(final String name) {

        final String[] names = name.split(" ");
        return names[names.length - 1];
    }
}
