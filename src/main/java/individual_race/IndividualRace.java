package individual_race;

import common.Race;

import java.io.IOException;
import java.util.Properties;

public class IndividualRace extends Race {

    // TODO variable number of prizes per category; open prizes in addition to gender categories; non-binary category; optional team prizes

    public IndividualRace(final String config_file_path) throws IOException {
        super(config_file_path);
    }

    public IndividualRace(final Properties properties) throws IOException {
        super(properties);
    }

    @Override
    protected void configure() throws IOException {
    }

    @Override
    public void processResults() throws IOException {
    }
}
