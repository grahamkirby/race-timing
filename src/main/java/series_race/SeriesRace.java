package series_race;

import common.Race;
import individual_race.*;

import java.io.IOException;
import java.util.*;

public class SeriesRace extends Race {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    SeriesRaceInput input;

    IndividualRace[] races;
    SeriesRaceResult[] overall_results;

    int minimum_number_of_races;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRace(String config_file_path) throws IOException {
        super(config_file_path);
    }

    public SeriesRace(final Properties properties) throws IOException {
        super(properties);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            new SeriesRace(args[0]).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureInputData();
    }

    @Override
    public void processResults() throws IOException {

        initialiseResults();

        calculateResults();
        allocatePrizes();

        printOverallResults();
        printPrizes();
        printCombined();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        super.readProperties();
        minimum_number_of_races = Integer.parseInt(properties.getProperty("MINIMUM_NUMBER_OF_RACES"));
    }

    private void configureHelpers() {

        input = new SeriesRaceInput(this);

    }

    private void configureInputData() throws IOException {

        races = input.loadSeriesRaces();
    }

    private void initialiseResults() {

        Runner[] combined_runners = getCombinedRunners();
        overall_results = new SeriesRaceResult[combined_runners.length];
    }

    private Runner[] getCombinedRunners() {

        Set<Runner> runners = new HashSet<>();

        for (final IndividualRace individual_race : races)
            for (final IndividualRaceResult result : individual_race.getOverallResults())
                runners.add(result.entry.runner);

        return runners.toArray(new Runner[0]);
    }

    private void calculateResults() {

        // Check dead heats.

        final Runner[] combined_runners = getCombinedRunners();

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = getOverallResult(combined_runners[i]);

        Arrays.sort(overall_results);
    }

    private SeriesRaceResult getOverallResult(final Runner runner) {

        final SeriesRaceResult result = new SeriesRaceResult(runner, this);

        for (int i = 0; i < races.length; i++) {

            IndividualRace individual_race = races[i];

            if (individual_race != null)
                result.scores[i] = calculateRaceScore(individual_race, runner);
        }

        return result;
    }

    private int calculateRaceScore(final IndividualRace individual_race, final Runner runner) {
        return 0;
    }

    private void allocatePrizes() {

    }

    private void printOverallResults() throws IOException {

    }

    private void printPrizes() throws IOException {

    }

    private void printCombined() throws IOException {

    }
}
