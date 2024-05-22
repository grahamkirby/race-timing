package minitour;

import common.Category;
import common.Race;
import individual_race.IndividualRace;
import individual_race.Runner;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static common.Race.format;
import static series_race.SeriesRaceOutputHTML.htmlEncode;

public class MinitourRaceOutputText extends MinitourRaceOutput {

    public MinitourRaceOutputText(final MinitourRace race) {
        super(race);
    }

    @Override
    public void printOverallResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printCombined() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printPrizes() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : race.categories.getCategoriesInReportOrder())
                printPrizes(category, writer);
        }
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final String header = "Category: " + category.getLongName();

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        printResults(getMinitourRaceResults(category), new ResultPrinter() {

            @Override
            public void printResult(MinitourRaceResult result) throws IOException {
                writer.append(String.valueOf(result.position_string)).append(": ").
                        append(result.runner.name).append(" (").
                        append(result.runner.club).append(") ").
                        append(Race.format(result.duration())).append("\n");
            }

            @Override
            public void printNoResults() throws IOException {
                writer.append("No results\n");
            }
        });

        writer.append("\n\n");
    }
}
