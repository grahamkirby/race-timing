package relay_race;

import common.RaceOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;

public abstract class RelayRaceOutput extends RaceOutput {

    String detailed_results_filename, collated_times_filename;

    public RelayRaceOutput(final RelayRace race) {

        super(race);
        configure();
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    protected void readProperties() {

        race_name_for_results = race.getProperties().getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = race.getProperties().getProperty("RACE_NAME_FOR_FILENAMES");
        year = race.getProperties().getProperty("YEAR");
    }

    protected void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;
        collated_times_filename = "times_collated";

        Path workingDirectoryPath = race.getWorkingDirectoryPath();
        output_directory_path = workingDirectoryPath.resolve("output");
    }

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++)
            printLegResults(leg);
    }

    public void printLegResults(int leg) throws IOException {
        throw new UnsupportedOperationException();
    }

    Duration sumDurationsUpToLeg(final LegResult[] leg_results, final int leg) {

        Duration total = leg_results[0].duration();
        for (int i = 1; i < leg; i++)
            total = total.plus(leg_results[i].duration());
        return total;
    }

    LegResult[] getLegResults(final int leg_number) {

        final LegResult[] leg_results = new LegResult[((RelayRace)race).overall_results.length];

        for (int i = 0; i < leg_results.length; i++)
            leg_results[i] = ((RelayRace)race).overall_results[i].leg_results[leg_number-1];

        // Sort in order of increasing overall leg time, as defined in LegResult.compareTo().
        // Ordering for DNF results doesn't matter since they're omitted in output.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.
        // OutputCSV.printLegResults deals with dead heats.
        Arrays.sort(leg_results);

        return leg_results;
    }

    void addMassStartAnnotation(final OutputStreamWriter writer, final LegResult leg_result, final int leg) throws IOException {

        // Adds e.g. "(M3)" after names of runners that started in leg 3 mass start.
        if (leg_result.in_mass_start) {

            // Find the next mass start.
            int mass_start_leg = leg;
            while (!((RelayRace)race).mass_start_legs[mass_start_leg-1])
                mass_start_leg++;

            writer.append(" (M").append(String.valueOf(mass_start_leg)).append(")");
        }
    }
}
