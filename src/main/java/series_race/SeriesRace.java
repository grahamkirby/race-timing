package series_race;

import common.Race;
import common.RaceResult;
import common.Runner;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class SeriesRace extends Race {

    public List<IndividualRace> races;
    public List<Runner> combined_runners;

    public int category_prizes;
    public int minimum_number_of_races;

    public SeriesRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void configureInputData() throws IOException {

        races = ((SeriesRaceInput)input).loadRaces();
    }

    public void processResults() throws IOException {

        initialiseResults();

        calculateResults();
        allocatePrizes();

        printOverallResults();
        printPrizes();
        printCombined();
    }

    @Override
    public void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();
    }

    public void initialiseResults() {

        combined_runners = new ArrayList<>();

        for (final IndividualRace individual_race : races)
            if (individual_race != null)
                for (final RaceResult result : individual_race.getOverallResults()) {

                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (!combined_runners.contains(runner))
                        combined_runners.add(runner);
                }
    }

    protected void readProperties() {

        category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(3)));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static List<Runner> getCombinedRunners(final List<IndividualRace> individual_races) {

        final List<Runner> runners = new ArrayList<>();

        for (final IndividualRace individual_race : individual_races)
            if (individual_race != null)
                for (final RaceResult result : individual_race.getOverallResults()) {
                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (!runners.contains(runner))
                        runners.add(runner);
                }

        return runners;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void configureHelpers();
    public abstract void configureCategories();

    public abstract void calculateResults() throws IOException;
    public abstract void allocatePrizes() throws IOException;
    public abstract void printOverallResults() throws IOException;
    public abstract void printPrizes() throws IOException;
    public abstract void printCombined() throws IOException;
}
