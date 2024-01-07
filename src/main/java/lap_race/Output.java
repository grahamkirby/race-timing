package lap_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public abstract class Output {

    public static final String DNF_STRING = "DNF";

    public static final List<Category> CATEGORY_REPORT_ORDER = Arrays.asList(
            Category.FEMALE_SENIOR,
            Category.OPEN_SENIOR,
            Category.FEMALE_40,
            Category.OPEN_40,
            Category.FEMALE_50,
            Category.OPEN_50,
            Category.FEMALE_60,
            Category.OPEN_60,
            Category.MIXED_SENIOR,
            Category.MIXED_40);

    final Results results;

    String year;
    String race_name_for_results;
    String race_name_for_filenames;
    String overall_results_filename;
    String detailed_results_filename;
    String prizes_filename;
    Path output_directory_path;

    public Output(final Results results) {

        this.results = results;
        configure();
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    private void readProperties() {

        race_name_for_results = results.properties.getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = results.properties.getProperty("RACE_NAME_FOR_FILENAMES");
        year = results.properties.getProperty("YEAR");
    }

    private void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;

        output_directory_path = results.working_directory_path.resolve("output");
    }

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= results.number_of_legs; leg++)
            printLegResults(leg);
    }

    Duration sumDurationsUpToLeg(final LegResult[] leg_results, final int leg) {

        Duration total = leg_results[0].duration();
        for (int i = 1; i < leg; i++)
            total = total.plus(leg_results[i].duration());
        return total;
    }

    LegResult[] getLegResults(final int leg) {

        final LegResult[] leg_results = new LegResult[results.overall_results.length];

        for (int i = 0; i < leg_results.length; i++)
            leg_results[i] = results.overall_results[i].leg_results[leg-1];

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
            while (!results.mass_start_legs[mass_start_leg-1])
                mass_start_leg++;

            writer.append(" (M").append(String.valueOf(mass_start_leg)).append(")");
        }
    }

    public static String format(final Duration duration) {

        final long s = duration.getSeconds();
        return String.format("0%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    public abstract void printOverallResults() throws IOException;
    public abstract void printDetailedResults() throws IOException;
    public abstract void printLegResults(final int leg) throws IOException;
    public abstract void printPrizes() throws IOException;
}
