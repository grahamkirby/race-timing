package series_race;

import common.Category;
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

    public static final int MAX_RACE_SCORE = 200;
    private static final String DEFAULT_OPEN_PRIZES = "3";
    private static final String DEFAULT_CATEGORY_PRIZES = "1";

    SeriesRaceInput input;
    SeriesRaceOutput output_CSV, output_text;
    SeriesRacePrizes prizes;

    IndividualRace[] races;
    SeriesRaceResult[] overall_results;
    Map<Category, List<Runner>> prize_winners = new HashMap<>();

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

        open_prizes = 3;
        category_prizes = 3;

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();
    }

    protected void configureCategories() {

        categories_in_decreasing_generality_order = SeriesRaceCategories.getCategoriesInDecreasingGeneralityOrder(open_category, open_prizes, category_prizes);
        categories_in_report_order = SeriesRaceCategories.getCategoriesInReportOrder(open_category, open_prizes, category_prizes);
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

    @Override
    protected int getDefaultOpenPrizes() {
        return 3;
    }

    @Override
    protected int getDefaultCategoryPrizes() {
        return 3;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        super.readProperties();
        minimum_number_of_races = Integer.parseInt(properties.getProperty("MINIMUM_NUMBER_OF_RACES"));
    }

    private void configureHelpers() {

        input = new SeriesRaceInput(this);

        output_CSV = new SeriesRaceOutputCSV(this);
        output_text = new SeriesRaceOutputText(this);

        prizes = new SeriesRacePrizes(this);
    }

    private void configureInputData() throws IOException {

        races = input.loadSeriesRaces();

        Set<String> runner_names = getRunnerNames(races);
        for (String runner_name : runner_names) {
            List<String> clubs_for_runner = getClubsForRunner(runner_name);
            List<String> defined_clubs = getDefinedClubs(clubs_for_runner);
            int number_of_defined_clubs = defined_clubs.size();
            int number_of_undefined_clubs = clubs_for_runner.size() - number_of_defined_clubs;
            if (number_of_defined_clubs == 1 && number_of_undefined_clubs > 0) {
                String defined_club = defined_clubs.get(0);

                for (IndividualRace race : races) {
                    if (race != null)
                        for (IndividualRaceResult result : race.getOverallResults()) {
                            Runner runner = result.entry.runner;
                            if (runner.name.equals(runner_name) && runner.club.equals("?")) {
                                runner.club = defined_club;
                            }
                        }
                }
            }
        }
    }

    private List<String> getDefinedClubs(List<String> clubsForRunner) {
        return clubsForRunner.stream().filter(club -> !club.equals("?")).toList();
    }

    private List<String> getClubsForRunner(String runner_name) {
        Set<String> clubs = new HashSet<>();
        for (IndividualRace race : races) {
            if (race != null)
                for (IndividualRaceResult result : race.getOverallResults()) {
                    Runner runner = result.entry.runner;
                    if (runner.name.equals(runner_name)) clubs.add(runner.club);
                }
        }

        return clubs.stream().toList();
    }

    private Set<String> getRunnerNames(IndividualRace[] races) {
        Set<String> names = new HashSet<>();
        for (IndividualRace race : races) {
            if (race != null)
                for (IndividualRaceResult result : race.getOverallResults()) {
                    Runner runner = result.entry.runner;
                    names.add(runner.name);
                }
        }

        return names;
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

        int score = MAX_RACE_SCORE;

        final String gender = getGender(runner);

        for (IndividualRaceResult result : individual_race.getOverallResults()) {

            final Runner result_runner = result.entry.runner;

            if (result_runner.equals(runner)) return Math.max(score, 0);
            if (gender.equals(getGender(result_runner))) score--;
        }

        return 0;
    }

    private static String getGender(final Runner runner) {
        return runner.category.getGender();
    }

    private void allocatePrizes() {

        prizes.allocatePrizes();
    }

    private void printOverallResults() throws IOException {

        output_CSV.printOverallResults();
    }

    private void printPrizes() throws IOException {

        output_text.printPrizes();
    }

    private void printCombined() throws IOException {

    }

    public SeriesRaceResult[] getOverallResults() {
        return overall_results;
    }

    public int findIndexOfRunner(Runner runner) {

        for (int i = 0; i < overall_results.length; i++) {
            if (runner.equals(overall_results[i].runner)) return i;
        }
        return -1;
    }
}
