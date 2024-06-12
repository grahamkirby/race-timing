package relay_race;

import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RelayRaceOutputCSV extends RelayRaceOutput {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    public RelayRaceOutputCSV(final RelayRace results) {
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
    public void printDetailedResults() throws IOException {

        final Path detailed_results_csv_path = output_directory_path.resolve(detailed_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_csv_path))) {

            printDetailedResultsHeader(csv_writer);
            printDetailedResults(csv_writer);
        }
    }

    @Override
    public void printLegResults(final int leg_number) throws IOException {

        final Path leg_results_csv_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg_number + "_" + year + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_csv_path))) {

            printLegResultsHeader(csv_writer, leg_number);

            final List<LegResult> leg_results = getLegResults(leg_number);

            // Deal with dead heats in legs after the first.
            setPositionStrings(leg_results, leg_number > 1);

            printLegResults(csv_writer, leg_results);
        }
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER).append("Total\n");
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

            final RelayRaceResult result = (RelayRaceResult) r;

            if (!result.dnf()) writer.append(result.position_string);

            writer.append(",").
                    append(String.valueOf(result.entry.bib_number)).append(",").
                    append(result.entry.team.name).append(",").
                    append(result.entry.team.category.getLongName()).append(",").
                    append(result.dnf() ? "DNF" : format(result.duration())).append("\n");
        }

        @Override
        public void printNoResults() {
        }
    }

    private void printDetailedResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

            writer.append("Runners ").append(String.valueOf(leg_number)).append(",Leg ").append(String.valueOf(leg_number)).append(",");
            if (leg_number < ((RelayRace)race).number_of_legs) writer.append("Split ").append(String.valueOf(leg_number)).append(",");
        }

        writer.append("Total\n");
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        for (int result_index = 0; result_index < race.getOverallResults().size(); result_index++)
            printDetailedResult(writer, result_index);
    }

    private void printDetailedResult(final OutputStreamWriter writer, final int result_index) throws IOException {

        final RelayRaceResult result = (RelayRaceResult) race.getOverallResults().get(result_index);

        if (!result.dnf()) writer.append(String.valueOf(result_index + 1));

        writer.append(",");
        writer.append(String.valueOf(result.entry.bib_number)).append(",");
        writer.append(result.entry.team.name).append(",");
        writer.append(result.entry.team.category.getLongName()).append(",");

        printLegDetails(writer, result, result.entry.team);

        writer.append("\n");
    }

    private void printLegDetails(final OutputStreamWriter writer, final RelayRaceResult result, final Team team) throws IOException {

        boolean any_previous_leg_dnf = false;

        for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

            final LegResult leg_result = result.leg_results.get(leg_number - 1);

            writer.append(team.runners[leg_number-1]);
            addMassStartAnnotation(writer, leg_result, leg_number);

            writer.append(",");
            writer.append(leg_result.DNF ? DNF_STRING : format(leg_result.duration())).append(",");
            writer.append(leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : format(sumDurationsUpToLeg(result.leg_results, leg_number)));

            if (leg_number < ((RelayRace)race).number_of_legs) writer.append(",");
            if (leg_result.DNF) any_previous_leg_dnf = true;
        }
    }

    private void printLegResultsHeader(final OutputStreamWriter writer, final int leg_number) throws IOException {

        writer.append("Pos,Runner");
        if (((RelayRace)race).paired_legs.get(leg_number-1)) writer.append("s");
        writer.append(",Time\n");
    }

    private void printLegResults(final OutputStreamWriter writer, final List<LegResult> leg_results) throws IOException {

        for (final LegResult leg_result : leg_results)
            printLegResult(writer, leg_result);
    }

    private void printLegResult(final OutputStreamWriter writer, final LegResult leg_result) throws IOException {

        if (!leg_result.DNF) {
            writer.append(leg_result.position_string).append(",");
            writer.append(leg_result.entry.team.runners[leg_result.leg_number - 1]).append(",");
            writer.append(format(leg_result.duration())).append("\n");
        }
    }
}
