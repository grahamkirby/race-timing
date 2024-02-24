package individual_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class IndividualRaceOutputCSV extends IndividualRaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time";

    public IndividualRaceOutputCSV(final IndividualRace results) {
        super(results);
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

        writer.append(OVERALL_RESULTS_HEADER).append("\n");
    }

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        for (int i = 0; i < race.overall_results.length; i++) {

            final Result overall_result = race.overall_results[i];

            if (!overall_result.dnf()) writer.append(String.valueOf(i + 1));

            writer.append(",").
                    append(String.valueOf(overall_result.runner.bib_number)).append(",").
                    append(overall_result.runner.name).append(",").
                    append(IndividualRace.normaliseClubName(overall_result.runner.club)).append(",").
                    append(overall_result.runner.category.shortName()).append(",").
                    append(overall_result.dnf() ? "DNF" : IndividualRaceOutput.format(overall_result.duration())).append("\n");
        }
    }
}
