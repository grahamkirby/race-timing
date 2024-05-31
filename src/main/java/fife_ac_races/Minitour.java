package fife_ac_races;

import common.Category;
import common.JuniorRaceCategories;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import common.Runner;
import minitour.*;
import series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Minitour extends SeriesRace {

    public static final int DEFAULT_CATEGORY_PRIZES = 3;

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    MinitourRaceInput input;
    MinitourRaceOutput output_CSV, output_HTML, output_text, output_PDF;
    MinitourRacePrizes prizes;

    public MinitourRaceResult[] overall_results;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Minitour(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MinitourRace <config file path>");
        else
            new Minitour(Paths.get(args[0])).processResults();
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

        races = input.loadMinitourRaces();
    }

    @Override
    public void initialiseResults() {

        super.initialiseResults();
        overall_results = new MinitourRaceResult[combined_runners.length];
    }

    @Override
    public void calculateResults() {

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = getOverallResult(combined_runners[i]);

        // Ordering defined in MinitourRaceResult: sort by time then by runner name.
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

        for (int i = 0; i < races.length; i++) {

            final IndividualRace individual_race = races[i];

            if (individual_race != null)
                result.times[i] = getRaceTime(individual_race, runner);
        }

        return result;
    }

    private Duration getRaceTime(final IndividualRace individual_race, final Runner runner) {

        for (IndividualRaceResult result : individual_race.getOverallResults())
            if (result.entry.runner.equals(runner)) return result.duration();

        return null;
    }

    public MinitourRaceResult[] getOverallResults() {

        return overall_results;
    }

    public MinitourRaceResult[] getResultsByCategory(List<Category> categories_required) {

        final Predicate<MinitourRaceResult> category_filter = minitourRaceResult -> categories_required.contains(minitourRaceResult.runner.category);

        return Stream.of(overall_results).filter(category_filter).toArray(MinitourRaceResult[]:: new);
    }

    public int findIndexOfRunner(Runner runner) {

        for (int i = 0; i < overall_results.length; i++)
            if (runner.equals(overall_results[i].runner)) return i;

        throw new RuntimeException("Runner not found: " + runner.name);
    }
}
