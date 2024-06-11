package fife_ac_races.minitour;

import common.Category;
import common.JuniorRaceCategories;
import common.Runner;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MinitourRace extends SeriesRace {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    MinitourRacePrizes prizes;

    public List<MinitourRaceResult> overall_results;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MinitourRace(final Path config_file_path) throws IOException {
        super(config_file_path);
        minimum_number_of_races = races.size();
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MinitourRace <config file path>");
        else
            new MinitourRace(Paths.get(args[0])).processResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configureHelpers() {

        input = new MinitourRaceInput(this);

        output_CSV = new MinitourRaceOutputCSV(this);
        output_HTML = new MinitourRaceOutputHTML(this);
        output_text = new MinitourRaceOutputText(this);
        output_PDF = new MinitourRaceOutputPDF(this);

        prizes = new MinitourRacePrizes(this);
    }

    @Override
    public void configureCategories() {

        categories = new JuniorRaceCategories(category_prizes);
    }

    @Override
    public void configureInputData() throws IOException {

        races = input.loadRaces();
    }

    @Override
    public void initialiseResults() {

        super.initialiseResults();
        overall_results = new ArrayList<>();
    }

    @Override
    public void calculateResults() {

        for (final Runner runner : combined_runners)
            overall_results.add(getOverallResult(runner));

        // Sort by time then by runner name.
        overall_results.sort(MinitourRaceResult::compareTo);
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

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    @Override
    public void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    private MinitourRaceResult getOverallResult(final Runner runner) {

        final MinitourRaceResult result = new MinitourRaceResult(runner, this);

        for (int i = 0; i < races.size(); i++) {

            final IndividualRace individual_race = races.get(i);

            if (individual_race != null)
                result.times.set(i, getRaceTime(individual_race, runner));
        }

        return result;
    }

    private Duration getRaceTime(final IndividualRace individual_race, final Runner runner) {

        for (IndividualRaceResult result : individual_race.getOverallResults())
            if (result.entry.runner.equals(runner)) return result.duration();

        return null;
    }

    public List<MinitourRaceResult> getOverallResults() {

        return overall_results;
    }

    public List<MinitourRaceResult> getResultsByCategory(List<Category> categories_required) {

        final Predicate<MinitourRaceResult> category_filter = minitourRaceResult -> categories_required.contains(minitourRaceResult.runner.category);

        return overall_results.stream().filter(category_filter).toList();
    }

    public int findIndexOfRunner(Runner runner) {

        for (int i = 0; i < overall_results.size(); i++)
            if (runner.equals(overall_results.get(i).runner)) return i;

        throw new RuntimeException("Runner not found: " + runner.name);
    }
}
