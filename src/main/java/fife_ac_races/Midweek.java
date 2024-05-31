package fife_ac_races;

import common.Runner;
import common.SeniorRaceCategories;
import individual_race.*;
import series_race.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Midweek extends SeriesRace {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int MAX_RACE_SCORE = 200;

    SeriesRaceInput input;
    SeriesRaceOutput output_CSV, output_HTML, output_text;
    SeriesRacePrizes prizes;

    SeriesRaceResult[] overall_results;

    public boolean open_category;
    public int open_prizes;

    public int minimum_number_of_races;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Midweek(Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java SeriesRace <config file path>");
        else {
            new Midweek(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        super.readProperties();
        minimum_number_of_races = Integer.parseInt(properties.getProperty("MINIMUM_NUMBER_OF_RACES"));

        open_category = Boolean.parseBoolean(getPropertyWithDefault("OPEN_CATEGORY", "true"));
        open_prizes = Integer.parseInt(getPropertyWithDefault("OPEN_PRIZES", String.valueOf(3)));
    }

    @Override
    public void configureHelpers() {

        input = new SeriesRaceInput(this);

        output_CSV = new SeriesRaceOutputCSV(this);
        output_HTML = new SeriesRaceOutputHTML(this);
        output_text = new SeriesRaceOutputText(this);

        prizes = new SeriesRacePrizes(this);
    }

    @Override
    public void configureCategories() {

        categories = new SeniorRaceCategories(open_category, open_prizes, category_prizes);
    }

    @Override
    public void configureInputData() throws IOException {

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

    @Override
    public void initialiseResults() {

        super.initialiseResults();
        overall_results = new SeriesRaceResult[combined_runners.length];
    }

    @Override
    public void calculateResults() {

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = getOverallResult(combined_runners[i]);

        Arrays.sort(overall_results);
    }

    @Override
    public void allocatePrizes() {

        prizes.allocatePrizes();
    }

    @Override
    public void printOverallResults() throws IOException {

        output_CSV.printOverallResults();
        output_HTML.printOverallResults();
    }

    @Override
    public void printPrizes() throws IOException {

        output_text.printPrizes();
    }

    @Override
    public void printCombined() throws IOException {

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
