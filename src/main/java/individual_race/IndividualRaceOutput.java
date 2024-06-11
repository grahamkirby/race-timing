package individual_race;

import common.RaceOutput;
import java.util.Objects;

public abstract class IndividualRaceOutput extends RaceOutput {

    String alternative_output_directory_path;

    public IndividualRaceOutput(final IndividualRace race) {
        super(race);
    }

    protected void readProperties() {

        super.readProperties();

        alternative_output_directory_path = race.getProperties().getProperty("ALTERNATIVE_OUTPUT_DIRECTORY");
    }

    protected void constructFilePaths() {

        super.constructFilePaths();

        output_directory_path = race.getWorkingDirectoryPath().resolve(Objects.requireNonNullElse(alternative_output_directory_path, "output"));
    }
}
