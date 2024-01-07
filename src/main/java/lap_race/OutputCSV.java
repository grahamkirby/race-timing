package lap_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputCSV extends Output {

    public static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    public OutputCSV(final Results results) {
        super(results);
    }

    @Override
    public void printPrizes() {

        throw new UnsupportedOperationException();
    }

    @Override
    public void printOverallResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsCSVHeader(csv_writer);
            printOverallResultsCSV(csv_writer);
        }
    }

    @Override
    public void printDetailedResults() throws IOException {

        final Path detailed_results_csv_path = output_directory_path.resolve(detailed_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_csv_path))) {

            printDetailedResultsCSVHeader(csv_writer);
            printDetailedResultsCSV(csv_writer);
        }
    }

    @Override
    void printLegResults(final int leg) throws IOException {

        final Path leg_results_csv_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_csv_path))) {

            printLegResultsCSVHeader(leg, csv_writer);
            printLegResultsCSV(getLegResults(leg), csv_writer);
        }
    }

    private void printOverallResultsCSVHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER).append("Total\n");
    }

    private void printOverallResultsCSV(final OutputStreamWriter writer) throws IOException {

        for (int i = 0; i < results.overall_results.length; i++) {

            if (!results.overall_results[i].dnf()) writer.append(String.valueOf(i + 1));
            writer.append(",").append(String.valueOf(results.overall_results[i])).append("\n");
        }
    }

    private void printDetailedResultsCSVHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

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
                writer.append(leg_result.DNF ? DNF_STRING : OverallResult.format(leg_result.duration())).append(",");
                writer.append(leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : OverallResult.format(sumDurationsUpToLeg(result.leg_results, leg)));

                if (leg < results.number_of_legs) writer.append(",");
                if (leg_result.DNF) any_previous_leg_dnf = true;
            }

            writer.append("\n");
        }
    }

    private void printLegResultsCSVHeader(final int leg, final OutputStreamWriter writer) throws IOException {

        writer.append("Pos,Runner");
        if (results.paired_legs[leg-1]) writer.append("s");
        writer.append(",Time\n");
    }

    private void printLegResultsCSV(final LegResult[] leg_results, final OutputStreamWriter writer) throws IOException {

        final int number_of_results = leg_results.length;

        // Deal with dead heats in legs 2-4.
        for (int i = 0; i < number_of_results; i++) {

            final LegResult result = leg_results[i];
            if (result.leg_number == 1) {
                result.position_string = String.valueOf(i + 1);
            }
            else {
                int j = i;

                while (j + 1 < number_of_results && result.duration().equals(leg_results[j + 1].duration())) j++;
                if (j > i) {
                    for (int k = i; k <= j; k++)
                        leg_results[k].position_string = i + 1 + "=";
                    i = j;
                } else
                    result.position_string = String.valueOf(i + 1);
            }
        }

        for (final LegResult leg_result : leg_results) {

            if (!leg_result.DNF) {
                writer.append(leg_result.position_string).append(",");
                writer.append(leg_result.toString()).append("\n");
            }
        }
    }
}
