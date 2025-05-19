package org.grahamkirby.race_timing.individual_race;

import java.io.IOException;
import java.nio.file.Path;

public class UntimedIndividualRace extends IndividualRace {

    public UntimedIndividualRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }
}
