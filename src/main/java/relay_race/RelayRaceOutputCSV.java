package relay_race;

import common.Race;
import common.RaceOutputCSV;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RelayRaceOutputCSV extends RaceOutputCSV {

    String detailed_results_filename, collated_times_filename;
    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    public RelayRaceOutputCSV(final Race race) {
        super(race);
        constructFilePaths();
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER).append("Total\n");
    }

    @Override
    protected ResultPrinter getResultPrinter(OutputStreamWriter writer) {
        return new ResultPrinterCSV(writer);
    }

    @Override
    protected boolean allowEqualPositions() {
        return false;
    }

    @Override
    public void printDetailedResults() throws IOException {

        final Path detailed_results_csv_path = output_directory_path.resolve(detailed_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_csv_path))) {

            printDetailedResultsHeader(csv_writer);
            printDetailedResults(csv_writer);
        }
    }

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++)
            printLegResults(leg);
    }

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

    List<LegResult> getLegResults(final int leg_number) {

        final List<LegResult> leg_results = new ArrayList<>();

        for (final RaceResult overall_result : race.getOverallResults())
            leg_results.add(((RelayRaceResult)overall_result).leg_results.get(leg_number - 1));

        // Sort in order of increasing overall leg time, as defined in LegResult.compareTo().
        // Ordering for DNF results doesn't matter since they're omitted in output.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.
        // OutputCSV.printLegResults deals with dead heats.
        leg_results.sort(LegResult::compareTo);

        return leg_results;
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

    Duration sumDurationsUpToLeg(final List<LegResult> leg_results, final int leg) {

        Duration total = Duration.ZERO;
        for (int i = 0; i < leg; i++)
            total = total.plus(leg_results.get(i).duration());
        return total;
    }

    protected void constructFilePaths() {

        super.constructFilePaths();

        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
        collated_times_filename = "times_collated";
    }

    void addMassStartAnnotation(final OutputStreamWriter writer, final LegResult leg_result, final int leg) throws IOException {

        // Adds e.g. "(M3)" after names of runners that started in leg 3 mass start.
        if (leg_result.in_mass_start) {

            // Find the next mass start.
            int mass_start_leg = leg;
            while (!((RelayRace)race).mass_start_legs.get(mass_start_leg-1))
                mass_start_leg++;

            writer.append(" (M").append(String.valueOf(mass_start_leg)).append(")");
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
