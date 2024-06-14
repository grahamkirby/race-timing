package common.output;

import common.Race;
import common.RaceResult;
import common.categories.Category;
import series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class RaceOutputCSV extends RaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public RaceOutputCSV(Race race) {
        super(race);
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

        for (final Race.CategoryGroup category_group : race.getResultCategoryGroups())
            printCategoryResults(writer, category_group.category_names());
    }

    @Override
    public void printOverallResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsHeader(writer);
            printOverallResults(writer);
        }
    }

    @Override
    protected void printCategoryResults(final OutputStreamWriter writer, final List<String> category_names) throws IOException {

        final List<Category> category_list = category_names.stream().map(s -> race.categories.getCategory(s)).toList();
        final List<RaceResult> results = race.getResultsByCategory(category_list);

        setPositionStrings(results, allowEqualPositions());
        printResults(results, getResultPrinter(writer));
    }

    protected void printOverallResultsHeaderRootSeries(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : ((SeriesRace)race).races)
            if (individual_race != null)
                writer.append(",").
                        append(individual_race.getProperties().getProperty("RACE_NAME_FOR_RESULTS"));
    }
}
