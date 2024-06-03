package minitour;

import common.Category;
import common.Race;
import fife_ac_races.Minitour;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class MinitourRaceOutputCSV extends MinitourRaceOutput {

    public MinitourRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    public void printOverallResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsHeader(csv_writer);
            printOverallResults(csv_writer);
        }
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : ((Minitour)race).races)
            if (individual_race != null)
                writer.append(",").append(individual_race.getProperties().getProperty("RACE_NAME_FOR_RESULTS"));

        writer.append(",Total\n");
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

        printCategoryResults(writer, "FU9", "MU9");
        printCategoryResults(writer, "FU11", "MU11");
        printCategoryResults(writer, "FU13","MU13");
        printCategoryResults(writer, "FU15","MU15");
        printCategoryResults(writer, "FU18","MU18");
    }

    private void printCategoryResults(final OutputStreamWriter writer, final String... category_names) throws IOException {

        final List<Category> category_list = Arrays.stream(category_names).map(s -> race.categories.getCategory(s)).toList();
        final MinitourRaceResult[] category_results = ((Minitour)race).getResultsByCategory(category_list);

        printResults(category_results, new ResultPrinterCSV(writer));
    }

    record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final MinitourRaceResult result) throws IOException {

            writer.append(result.completedAllRacesSoFar() ? result.position_string : "-").append(",").
                    append(result.runner.name).append(",").
                    append(result.runner.club).append(",").
                    append(result.runner.category.getShortName()).append(",");

            for (final Duration time : result.times)
                writer.append(time != null ? format(time) : "-").append(",");

            writer.append(result.completedAllRacesSoFar() ? format(result.duration()) : "-").append("\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("No results\n");
        }
    }
}
