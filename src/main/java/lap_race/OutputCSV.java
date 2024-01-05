package lap_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputCSV {

    private final Results results;

    public OutputCSV(final Results results) throws IOException {
        this.results = results;
    }

    public void printOverallResultsCSV() throws IOException {

        final Path overall_results_csv_path = results.output_directory_path.resolve(results.overall_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            csv_writer.append(Results.OVERALL_RESULTS_HEADER).append("Total\n");

            for (int i = 0; i < results.overall_results.length; i++) {

                if (!results.overall_results[i].dnf()) csv_writer.append(String.valueOf(i + 1));
                csv_writer.append(",").append(String.valueOf(results.overall_results[i])).append("\n");
            }
        }
    }

    public void printDetailedResultsCSV() throws IOException {

        final Path detailed_results_csv_path = results.output_directory_path.resolve(results.detailed_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_csv_path))) {

             printDetailedResultsCSVHeader(csv_writer);
             printDetailedResultsCSV(csv_writer);
        }
    }

    private void printDetailedResultsCSVHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(Results.OVERALL_RESULTS_HEADER);

        for (int leg = 1; leg <= results.number_of_legs; leg++) {
            writer.append("Runners ").append(String.valueOf(leg)).append(",Leg ").append(String.valueOf(leg)).append(",");
            if (leg < results.number_of_legs) writer.append("Split ").append(String.valueOf(leg)).append(",");
        }

        writer.append("Total\n");
    }

    private void printDetailedResultsCSV(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final OverallResult result : results.overall_results) {

            final Team team = result.team;
            boolean any_previous_leg_dnf = false;

            if (!result.dnf()) writer.append(String.valueOf(position++));

            writer.append(",");
            writer.append(String.valueOf(team.bib_number)).append(",");
            writer.append(team.name).append(",");
            writer.append(team.category.toString()).append(",");

            for (int leg = 1; leg <= results.number_of_legs; leg++) {

                final LegResult leg_result = result.leg_results[leg-1];

                writer.append(team.runners[leg-1]);
                if (leg_result.in_mass_start) {
                    int mass_start_leg = leg;
                    while (!results.mass_start_legs[mass_start_leg-1]) {
                        mass_start_leg++;
                    }
                    writer.append(" (M").append(String.valueOf(mass_start_leg)).append(")");
                }
                writer.append(",");
                writer.append(leg_result.DNF ? Results.DNF_STRING : OverallResult.format(leg_result.duration())).append(",");
                writer.append(leg_result.DNF || any_previous_leg_dnf ? Results.DNF_STRING : OverallResult.format(results.sumDurationsUpToLeg(result.leg_results, leg)));

                if (leg < results.number_of_legs) writer.append(",");
                if (leg_result.DNF) any_previous_leg_dnf = true;
            }

            writer.append("\n");
        }
    }
}
