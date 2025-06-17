package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing_experimental.common.CommonRace;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceImpl;

import java.io.IOException;
import java.nio.file.Path;

public class IndividualRaceFactory {

    public static Race makeIndividualRace(final Path config_file_path) throws IOException {

        Race race = new CommonRace(config_file_path);

        RaceImpl single_race = new SingleRace(race);
        race.setRaceImpl(single_race);

        SingleRaceType individual_race = new IndividualRace(single_race);
        single_race.setRaceType(individual_race);

        return race;
    }
}
