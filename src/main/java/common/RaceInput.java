package common;

import individual_race.IndividualRace;

import java.io.IOException;
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

    public IndividualRace[] loadRaces() throws IOException {

        final IndividualRace[] races = new IndividualRace[race_config_paths.length];

        for (int i = 0; i < races.length; i++) {

            final Path relative_path = race_config_paths[i];

            if (!relative_path.toString().isEmpty())
                races[i] = getIndividualRace(relative_path, i + 1);

        }

        return races;
    }

    protected IndividualRace getIndividualRace(final Path relative_path, final int race_number) throws IOException {

        final Path individual_race_path = race.getWorkingDirectoryPath().resolve(relative_path);
        final IndividualRace individual_race = new IndividualRace(individual_race_path);

        configureIndividualRace(individual_race, race_number);
        individual_race.processResults(false);

        return individual_race;
    }

    protected abstract void configureIndividualRace(final IndividualRace individual_race, final int race_number) throws IOException;
}
