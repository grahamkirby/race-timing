package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing_experimental.common.CommonRace;
import org.grahamkirby.race_timing_experimental.common.Race;

import java.io.IOException;
import java.nio.file.Path;

public class IndividualRaceFactory {

    public static Race makeIndividualRace(final Path config_file_path) throws IOException {

        Race race = new CommonRace(config_file_path);

        return race;
    }


}
