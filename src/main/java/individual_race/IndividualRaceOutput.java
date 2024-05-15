package individual_race;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public abstract class IndividualRaceOutput {

    public static final String DNF_STRING = "DNF";

    final IndividualRace race;

    String year;
    String race_name_for_results;
    String race_name_for_filenames;
    String overall_results_filename;
    String prizes_filename;
    Path output_directory_path;

    String alternative_output_directory_path;

    public IndividualRaceOutput(final IndividualRace race) {

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
        alternative_output_directory_path = race.getProperties().getProperty("ALTERNATIVE_OUTPUT_DIRECTORY");
    }

    private void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;

        if (alternative_output_directory_path == null) {
            output_directory_path = race.getWorkingDirectoryPath().resolve("output");
        }
        else {
            output_directory_path = race.getWorkingDirectoryPath().resolve(alternative_output_directory_path);
        }
        int x = 2;
    }

    public static String format(final Duration duration) {

        final long s = duration.getSeconds();
        return String.format("0%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    public abstract void printOverallResults() throws IOException;
    public abstract void printPrizes() throws IOException;
    public abstract void printCombined() throws IOException;
}
