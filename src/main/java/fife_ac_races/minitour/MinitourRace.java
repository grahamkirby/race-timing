package fife_ac_races.minitour;

import common.RacePrizes;
import common.RaceResult;
import common.Runner;
import common.categories.Category;
import common.categories.JuniorRaceCategories;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class MinitourRace extends SeriesRace {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MinitourRace(final Path config_file_path) throws IOException {

        super(config_file_path);
        minimum_number_of_races = races.size();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MinitourRace <config file path>");
        else
            new MinitourRace(Paths.get(args[0])).processResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<CategoryGroup> getResultCategoryGroups() {

        return List.of(
                new CategoryGroup("U9", List.of("FU9", "MU9")),
                new CategoryGroup("U11", List.of("FU11", "MU11")),
                new CategoryGroup("U13", List.of("FU13", "MU13")),
                new CategoryGroup("U15", List.of("FU15", "MU15")),
                new CategoryGroup("U18", List.of("FU18", "MU18"))
        );
    }

    @Override
    public void configureHelpers() {

        input = new MinitourRaceInput(this);

        output_CSV = new MinitourRaceOutputCSV(this);
        output_HTML = new MinitourRaceOutputHTML(this);
        output_text = new MinitourRaceOutputText(this);
        output_PDF = new MinitourRaceOutputPDF(this);

        prizes = new RacePrizes(this);
    }

    @Override
    public void configureCategories() {

        categories = new JuniorRaceCategories(category_prizes);
    }

    @Override
    protected Comparator<RaceResult> getResultsSortComparator() {
        return MinitourRaceResult::compare;
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

    @Override
    public List<RaceResult> getResultsByCategory(final List<Category> categories_required) {

        final Predicate<RaceResult> category_filter = result -> categories_required.contains(((MinitourRaceResult)result).runner.category);

        return overall_results.stream().filter(category_filter).toList();
    }

    protected RaceResult getOverallResult(final Runner runner) {

        final MinitourRaceResult result = new MinitourRaceResult(runner, this);

        for (final IndividualRace individual_race : races)
            result.times.add(getRaceTime(individual_race, runner));

        return result;
    }

    private Duration getRaceTime(final IndividualRace individual_race, final Runner runner) {

        if (individual_race == null) return null;

        for (RaceResult result : individual_race.getOverallResults())
            if (((IndividualRaceResult)result).entry.runner.equals(runner)) return ((IndividualRaceResult)result).duration();

        return null;
    }
}
