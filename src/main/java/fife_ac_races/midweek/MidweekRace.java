package fife_ac_races.midweek;

import common.RacePrizes;
import common.RaceResult;
import common.Runner;
import common.categories.SeniorRaceCategories;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MidweekRace extends SeriesRace {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int MAX_RACE_SCORE = 200;

    private boolean open_category;
    private int open_prizes;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MidweekRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MidweekRace <config file path>");
        else {
            new MidweekRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected Comparator<RaceResult> getResultsSortComparator() {
        return MidweekRaceResult::compare;
    }

    @Override
    public void configureHelpers() {

        input = new MidweekRaceInput(this);

        output_CSV = new MidweekRaceOutputCSV(this);
        output_HTML = new MidweekRaceOutputHTML(this);
        output_text = new MidweekRaceOutputText(this);

        prizes = new RacePrizes(this);
    }

    @Override
    public void configureCategories() {

        categories = new SeniorRaceCategories(open_category, open_prizes, category_prizes);
    }

    @Override
    public void printPrizes() throws IOException {

        output_text.printPrizes();
    }

    @Override
    public void printCombined() throws IOException {
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final MidweekRaceResult result = new MidweekRaceResult(runner, this);

        for (final IndividualRace individual_race : races)
            result.scores.add(calculateRaceScore(individual_race, runner));

        return result;
    }

    @Override
    public void configureInputData() throws IOException {

        super.configureInputData();

        for (final String runner_name : getRunnerNames())
            checkClubsForRunner(runner_name);
    }

    @Override
    protected void readProperties() {

        super.readProperties();

        minimum_number_of_races = Integer.parseInt(getProperties().getProperty("MINIMUM_NUMBER_OF_RACES"));
        open_category = Boolean.parseBoolean(getPropertyWithDefault("OPEN_CATEGORY", "true"));
        open_prizes = Integer.parseInt(getPropertyWithDefault("OPEN_PRIZES", String.valueOf(3)));
    }

    private void checkClubsForRunner(final String runner_name) {

        // Where a runner name is associated with a single entry with a defined club
        // plus some other entries with no club defined, add the club to those entries.

        // Where a runner name is associated with multiple clubs, leave as is, under
        // assumption that they are separate runners.
        final List<String> clubs_for_runner = getRunnerClubs(runner_name);
        final List<String> defined_clubs = getDefinedClubs(clubs_for_runner);

        final int number_of_defined_clubs = defined_clubs.size();
        final int number_of_undefined_clubs = clubs_for_runner.size() - number_of_defined_clubs;

        if (number_of_defined_clubs == 1 && number_of_undefined_clubs > 0)
            recordClubForRunnerName(runner_name, defined_clubs.get(0));
    }

    private void recordClubForRunnerName(final String runner_name, final String defined_club) {

        for (final IndividualRace race : races)
            if (race != null)
                for (final RaceResult result : race.getOverallResults()) {

                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (runner.name.equals(runner_name) && runner.club.equals("?"))
                        runner.club = defined_club;
                }
    }

    private List<String> getDefinedClubs(final List<String> clubs) {
        return clubs.stream().filter(club -> !club.equals("?")).toList();
    }

    private List<String> getRunnerClubs(final String runner_name) {

        final Set<String> clubs = new HashSet<>();

        for (IndividualRace race : races) {
            if (race != null)
                for (final RaceResult result : race.getOverallResults()) {

                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (runner.name.equals(runner_name)) clubs.add(runner.club);
                }
        }

        return new ArrayList<>(clubs);
    }

    private List<String> getRunnerNames() {

        final Set<String> names = new HashSet<>();
        for (final IndividualRace race : races) {
            if (race != null)
                for (RaceResult result : race.getOverallResults()) {
                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    names.add(runner.name);
                }
        }

        return new ArrayList<>(names);
    }

    private int calculateRaceScore(final IndividualRace individual_race, final Runner runner) {

        if (individual_race == null) return -1;

        int score = MAX_RACE_SCORE;

        final String gender = runner.category.getGender();

        // The first finisher of each gender gets the maximum score, the next one less, and so on.
        for (final RaceResult result : individual_race.getOverallResults()) {

            final Runner result_runner = ((IndividualRaceResult)result).entry.runner;

            if (result_runner.equals(runner)) return Math.max(score, 0);
            if (gender.equals(result_runner.category.getGender())) score--;
        }

        // Runner didn't compete in this race.
        return 0;
    }
}
