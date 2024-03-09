package series_race;

import common.Race;
import individual_race.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SeriesRace extends Race {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    SeriesRaceInput input;
    SeriesRaceOutput output_CSV;

    IndividualRace[] races;
    SeriesRaceResult[] overall_results;

    int minimum_number_of_races;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            new SeriesRace(Paths.get(args[0])).processResults();
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

        output_CSV = new SeriesRaceOutputCSV(this);
    }

    private void configureInputData() throws IOException {

        races = input.loadSeriesRaces();
    }

    private void initialiseResults() {

        overall_results = new SeriesRaceResult[getCombinedRunners().length];
    }

    private Runner[] getCombinedRunners() {

        final Set<Runner> runners = new HashSet<>();

        for (final IndividualRace individual_race : races)
            if (individual_race != null)
                for (final IndividualRaceResult result : individual_race.getOverallResults()) {
                    if (!isDuplicate(result, runners))
                        runners.add(result.entry.runner);
                }

        return runners.toArray(new Runner[0]);
    }

    private boolean isDuplicate(IndividualRaceResult result, Set<Runner> runners) {

        String result_name = result.entry.runner.name();
        String result_club = result.entry.runner.club();

        for (Runner runner : runners) {
            if (result_name.equals(runner.name()) && result_club.equals(runner.club())) return true;
            if (result_name.equals(runner.name()) && result_club.equals("?")) return true;
        }
        return false;
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

            final IndividualRace individual_race = races[i];

            if (individual_race != null)
                result.scores[i] = calculateRaceScore(individual_race, runner);
        }

        return result;
    }

    private int calculateRaceScore(final IndividualRace individual_race, final Runner runner) {

        int score = 200;

        final String gender = getGender(runner);

        for (IndividualRaceResult result : individual_race.getOverallResults()) {

            final Runner result_runner = result.entry.runner;

            if (result_runner.equals(runner)) return Math.max(score, 0);
            if (gender.equals(getGender(result_runner))) score--;
        }

        return 0;
    }

    private static String getGender(final Runner runner) {
        return ((IndividualRaceCategory) runner.category()).getGender();
    }

    private void allocatePrizes() {

    }

    private void printOverallResults() throws IOException {

        output_CSV.printOverallResults();
    }

    private void printPrizes() throws IOException {

    }

    private void printCombined() throws IOException {

    }

    public SeriesRaceResult[] getOverallResults() {
        return overall_results;
    }
}
