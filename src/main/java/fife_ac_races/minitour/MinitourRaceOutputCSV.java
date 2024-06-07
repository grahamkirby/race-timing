package fife_ac_races.minitour;

import common.Category;
import common.RaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class MinitourRaceOutputCSV extends MinitourRaceOutput {

    public MinitourRaceOutputCSV(final SeriesRace race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        super.printOverallResultsHeader(writer);
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
        final MinitourRaceResult[] category_results = ((MinitourRace)race).getResultsByCategory(category_list);

        printResults(category_results, new ResultPrinterCSV(writer));
    }

    private record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            MinitourRaceResult result = (MinitourRaceResult) r;
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
