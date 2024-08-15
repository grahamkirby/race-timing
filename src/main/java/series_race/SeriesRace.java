package series_race;

import common.Race;
import common.RaceResult;
import common.Runner;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class SeriesRace extends Race {

    protected List<IndividualRace> races;
    protected List<Runner> combined_runners;

    protected int category_prizes;
    protected int minimum_number_of_races;

    public SeriesRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();
    }

    @Override
    public void processResults() throws IOException {

        initialiseResults();

        calculateResults();
        allocatePrizes();

        printOverallResults();
        printCombined();
        printPrizes();
    }

    public List<IndividualRace> getRaces() {
        return races;
    }

    public int getMinimumNumberOfRaces() {
        return minimum_number_of_races;
    }

    public int getNumberOfRacesTakenPlace() {

        int number_of_races_completed = 0;

        for (final Race individual_race : getRaces())
            if (individual_race != null) number_of_races_completed++;

        return number_of_races_completed;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configureInputData() throws IOException {

        races = ((SeriesRaceInput)input).loadRaces();
    }

    protected void readProperties() {

        category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(3)));
    }

    private void initialiseResults() {

        combined_runners = new ArrayList<>();

        for (final IndividualRace individual_race : races)
            if (individual_race != null)
                for (final RaceResult result : individual_race.getOverallResults()) {

                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (!combined_runners.contains(runner))
                        combined_runners.add(runner);
                }
    }

    private void calculateResults() {

        for (final Runner runner : combined_runners)
            overall_results.add(getOverallResult(runner));

        overall_results.sort(getResultsSortComparator());
    }

    private void printOverallResults() throws IOException {

        output_CSV.printOverallResults();
        output_HTML.printOverallResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract Comparator<RaceResult> getResultsSortComparator();
    protected abstract RaceResult getOverallResult(final Runner runner);

    protected abstract void configureHelpers();
    protected abstract void configureCategories();

    protected abstract void printPrizes() throws IOException;
    protected abstract void printCombined() throws IOException;


}
