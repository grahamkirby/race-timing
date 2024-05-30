package series_race;

import common.RaceOutput;
import java.io.IOException;

public abstract class SeriesRaceOutput extends RaceOutput {

    public SeriesRaceOutput(final SeriesRace race) {

        super(race);
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
    public abstract void printCombined() throws IOException;
}
