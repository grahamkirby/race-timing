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

    @Override
    public boolean sameEntrant(final RaceResult other) {
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

    protected int compareRunnerNameTo(final SeriesRaceResult o) {

        final int last_name_comparison = getLastName(runner.name).compareTo(getLastName(o.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : getFirstName(runner.name).compareTo(getFirstName(o.runner.name));
    }

    public boolean shouldDisplayPosition() {

        final SeriesRace series_race = (SeriesRace) race;
        final int number_of_races_taken_place = series_race.getNumberOfRacesTakenPlace();

        return number_of_races_taken_place < series_race.getRaces().size() || completed();
    }
}
