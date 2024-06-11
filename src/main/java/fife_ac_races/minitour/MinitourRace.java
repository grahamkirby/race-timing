package fife_ac_races.minitour;

import common.Category;
import common.JuniorRaceCategories;
import common.RaceResult;
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
        overall_results.sort(MinitourRaceResult::compare);
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

        for (RaceResult result : individual_race.getOverallResults())
            if (((IndividualRaceResult)result).entry.runner.equals(runner)) return ((IndividualRaceResult)result).duration();

        return null;
    }

    public List<RaceResult> getResultsByCategory(List<Category> categories_required) {

        final Predicate<RaceResult> category_filter = result -> categories_required.contains(((MinitourRaceResult)result).runner.category);

        return overall_results.stream().filter(category_filter).toList();
    }
}
