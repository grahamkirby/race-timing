package minitour;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public abstract class MinitourRaceOutput {

    public static final String DNF_STRING = "DNF";

    final MinitourRace race;

    String year;
    String race_name_for_results;
    String race_name_for_filenames;
    String overall_results_filename;
    String prizes_filename;
    Path output_directory_path;

    public MinitourRaceOutput(final MinitourRace race) {

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
        prizes_filename = race_name_for_filenames + "_prizes_" + year;

        output_directory_path = race.getWorkingDirectoryPath().resolve("output");
    }

    public abstract void printOverallResults() throws IOException;
    public abstract void printPrizes() throws IOException;
    public abstract void printCombined() throws IOException;
}
