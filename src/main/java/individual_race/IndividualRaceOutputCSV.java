package individual_race;

import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndividualRaceOutputCSV extends IndividualRaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time";

    public IndividualRaceOutputCSV(final IndividualRace results) {
        super(results);
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

        writer.append(OVERALL_RESULTS_HEADER).append("\n");
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

        final List<RaceResult> results = race.getOverallResults();

        setPositionStrings(results, false);
        printResults(results, new ResultPrinterCSV(writer));
    }

    private record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final IndividualRaceResult result = (IndividualRaceResult)r;

            if (!result.dnf()) {
                writer.append(String.valueOf(result.position_string));

                writer.append(",").
                        append(String.valueOf(result.entry.bib_number)).append(",").
                        append(result.entry.runner.name).append(",").
                        append((result.entry.runner.club)).append(",").
                        append(result.entry.runner.category.getShortName()).append(",").
                        append(result.dnf() ? "DNF" : format(result.duration())).append("\n");
            }
        }

        @Override
        public void printNoResults() {
        }
    }
}
