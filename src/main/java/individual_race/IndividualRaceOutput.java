package individual_race;

import common.RaceOutput;
import java.util.Objects;

public abstract class IndividualRaceOutput extends RaceOutput {

    String alternative_output_directory_path;

    public IndividualRaceOutput(final IndividualRace race) {

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
        alternative_output_directory_path = race.getProperties().getProperty("ALTERNATIVE_OUTPUT_DIRECTORY");
    }

    protected void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;

        output_directory_path = race.getWorkingDirectoryPath().resolve(Objects.requireNonNullElse(alternative_output_directory_path, "output"));
    }
}
