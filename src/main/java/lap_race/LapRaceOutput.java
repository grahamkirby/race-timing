package lap_race;

import common.Category;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public abstract class LapRaceOutput {

    public static final String DNF_STRING = "DNF";

//    public static final List<Category> CATEGORY_REPORT_ORDER = Arrays.asList(
//            LapRaceCategory.FEMALE_SENIOR,
//            LapRaceCategory.OPEN_SENIOR,
//            LapRaceCategory.FEMALE_40,
//            LapRaceCategory.OPEN_40,
//            LapRaceCategory.FEMALE_50,
//            LapRaceCategory.OPEN_50,
//            LapRaceCategory.FEMALE_60,
//            LapRaceCategory.OPEN_60,
//            LapRaceCategory.MIXED_SENIOR,
//            LapRaceCategory.MIXED_40);

    final LapRace race;

    String year;
    String race_name_for_results;
    String race_name_for_filenames;
    String overall_results_filename, detailed_results_filename, prizes_filename, collated_times_filename;
    Path output_directory_path;

    public LapRaceOutput(final LapRace race) {

        this.race = race;
        configure();
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    private void readProperties() {

        race_name_for_results = race.getProperties().getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = race.getProperties().getProperty("RACE_NAME_FOR_FILENAMES");
        year = race.getProperties().getProperty("YEAR");
    }

    private void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;
        collated_times_filename = "times_collated";

        output_directory_path = race.getWorkingDirectoryPath().resolve("output");
    }

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= race.number_of_legs; leg++)
            printLegResults(leg);
    }

    Duration sumDurationsUpToLeg(final LegResult[] leg_results, final int leg) {

        Duration total = leg_results[0].duration();
        for (int i = 1; i < leg; i++)
            total = total.plus(leg_results[i].duration());
        return total;
    }

    LegResult[] getLegResults(final int leg) {

        final LegResult[] leg_results = new LegResult[race.overall_results.length];

        for (int i = 0; i < leg_results.length; i++)
            leg_results[i] = race.overall_results[i].leg_results[leg-1];

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
            while (!race.mass_start_legs[mass_start_leg-1])
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
    public abstract void printCombined() throws IOException;
}
