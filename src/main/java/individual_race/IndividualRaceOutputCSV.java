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

        for (int i = 0; i < race.getOverallResults().size(); i++) {

            final IndividualRaceResult overall_result = (IndividualRaceResult)race.getOverallResults().get(i);

            if (!overall_result.DNF) {
                writer.append(String.valueOf(i + 1));

                writer.append(",").
                        append(String.valueOf(overall_result.entry.bib_number)).append(",").
                        append(overall_result.entry.runner.name).append(",").
                        append((overall_result.entry.runner.club)).append(",").
                        append(overall_result.entry.runner.category.getShortName()).append(",").
                        append(overall_result.DNF ? "DNF" : format(overall_result.duration())).append("\n");
            }
        }
    }
}
