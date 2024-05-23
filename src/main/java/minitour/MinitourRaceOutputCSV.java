package minitour;

import common.Category;
import common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static common.Race.format;

public class MinitourRaceOutputCSV extends MinitourRaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public MinitourRaceOutputCSV(final MinitourRace race) {
        super(race);
    }

    @Override
    public void printPrizes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printCombined() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printOverallResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsHeader(csv_writer);
            printOverallResults(csv_writer);
        }
    }

    private void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : race.races)
            if (individual_race != null)
                writer.append(",").append(individual_race.getProperties().getProperty("RACE_NAME_FOR_RESULTS"));

        writer.append(",Total\n");
    }

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        printCategoryResults(writer, "FU9", "MU9");
        printCategoryResults(writer, "FU11", "MU11");
        printCategoryResults(writer, "FU13","MU13");
        printCategoryResults(writer, "FU15","MU15");
        printCategoryResults(writer, "FU18","MU18");
    }

    private void printCategoryResults(final OutputStreamWriter writer, final String... category_names) throws IOException {

        final List<Category> category_list = Arrays.stream(category_names).map(s -> race.categories.getCategory(s)).toList();
        final MinitourRaceResult[] category_results = race.getCompletedResultsByCategory(category_list);

        printResults(category_results, new ResultPrinterCSV(writer));
    }

    record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final MinitourRaceResult result) throws IOException {

            writer.append(result.position_string).append(",").
                    append(result.runner.name).append(",").
                    append(result.runner.club).append(",").
                    append(result.runner.category.getShortName()).append(",");

            for (final Duration time : result.times)
                if (time != null) writer.append(format(time)).append(",");

            writer.append(format(result.duration())).append("\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("No results\n");
        }
    }
}
