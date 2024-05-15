package minitour;

import common.Category;
import common.Race;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import individual_race.Runner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MinitourRace extends Race {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    MinitourRaceInput input;
    MinitourRaceOutput output_CSV, output_HTML, output_text;
    MinitourRacePrizes prizes;

    IndividualRace[] races;
    MinitourRaceResult[] overall_results;
    Map<Category, List<Runner>> prize_winners = new HashMap<>();

    public int category_prizes;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MinitourRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            MinitourRace minitourRace = new MinitourRace(Paths.get(args[0]));
            minitourRace.configure();
            minitourRace.processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();
    }

    protected void configureCategories() {

        categories = new JuniorRaceCategories(category_prizes);
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

    protected int getDefaultCategoryPrizes() {
        return 3;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        super.readProperties();
        category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(getDefaultCategoryPrizes())));
    }

    private void configureHelpers() {

        input = new MinitourRaceInput(this);

        output_CSV = new MinitourRaceOutputCSV(this);
        output_HTML = new MinitourRaceOutputHTML(this);
        output_text = new MinitourRaceOutputText(this);

        prizes = new MinitourRacePrizes(this);
    }

    private void configureInputData() throws IOException {

        races = input.loadMinitourRaces();

    }

    private void initialiseResults() {

        overall_results = new MinitourRaceResult[getCombinedRunners().length];
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

        final String result_name = result.entry.runner.name;
        final String result_club = result.entry.runner.club;

        for (final Runner runner : runners) {
            if (result_name.equals(runner.name) && result_club.equals(runner.club)) return true;
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

    private MinitourRaceResult getOverallResult(final Runner runner) {

        final MinitourRaceResult result = new MinitourRaceResult(runner, this);

        for (int i = 0; i < races.length; i++) {

            final IndividualRace individual_race = races[i];

            if (individual_race != null)
                result.times[i] = getRaceTime(individual_race, runner);
        }

        return result;
    }

    private Duration getRaceTime(final IndividualRace individual_race, final Runner runner) {

        for (IndividualRaceResult result : individual_race.getOverallResults()) {

            final Runner result_runner = result.entry.runner;

            if (result_runner.equals(runner)) return result.duration();
        }

        return null;
    }

    private void allocatePrizes() {

        prizes.allocatePrizes();
    }

    private void printOverallResults() throws IOException {

        output_CSV.printOverallResults();
        output_HTML.printOverallResults();
    }

    private void printPrizes() throws IOException {

        output_text.printPrizes();
    }

    private void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    public MinitourRaceResult[] getOverallResults() {
        return overall_results;
    }

    public MinitourRaceResult[] getOverallResults(List<Category> categories_required) {

        return Stream.of(overall_results).filter(minitourRaceResult -> minitourRaceResult.completedAllRacesSoFar() && categories_required.contains(minitourRaceResult.runner.category)).toList().toArray(new MinitourRaceResult[0]);
    }

    public int findIndexOfRunner(Runner runner) {

        for (int i = 0; i < overall_results.length; i++) {
            if (runner.equals(overall_results[i].runner)) return i;
        }
        return -1;
    }
}
