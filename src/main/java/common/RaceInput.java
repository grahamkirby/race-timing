package common;

import fife_ac_races.Midweek;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class RaceInput {

    public final Race race;

    public Path[] race_config_paths;

    public RaceInput(final Race race) {

        this.race = race;
        configure();
    }

    public void configure() {

        readProperties();
    }

    public void readProperties() {

        race_config_paths = readRaceConfigPaths();
    }

    private Path[] readRaceConfigPaths() {

        final String[] race_strings = race.getProperties().getProperty("RACES").split(":", -1);

        final Path[] race_paths = new Path[race_strings.length];

        for (int i = 0; i < race_paths.length; i++)
            race_paths[i] = Paths.get(race_strings[i]);

        return race_paths;
    }
}
