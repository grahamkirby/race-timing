package series_race;

import common.RawResult;
import individual_race.IndividualRace;
import individual_race.IndividualRaceEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SeriesRaceInput {

    final SeriesRace race;

    Path[] race_config_paths;


    public SeriesRaceInput(final SeriesRace race) {

        this.race = race;
        configure();
    }

    private void configure() {

        readProperties();

    }

    private void readProperties() {

        race_config_paths = readRaceConfigPaths();
    }

    private Path[] readRaceConfigPaths() {

        String races_string = race.getProperties().getProperty("RACES");

        String[] race_strings = races_string.split(":", -1);

        Path[] race_paths = new Path[race_strings.length];

        for (int i = 0; i < race_paths.length; i++)
            race_paths[i] = Paths.get(race_strings[i]);

        return race_paths;
    }



    public IndividualRace[] loadSeriesRaces() {
        return null;
    }
}
