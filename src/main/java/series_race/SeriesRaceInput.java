package series_race;

import common.Race;
import common.RaceInput;
import fife_ac_races.Midweek;
import individual_race.IndividualRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SeriesRaceInput extends RaceInput {

    public SeriesRaceInput(final Midweek race) {

        super(race);
    }

//    @Override
//    public void readProperties() {
//
//        race_config_paths = readRaceConfigPaths();
//    }
//
//    private Path[] readRaceConfigPaths() {
//
//        final String[] race_strings = race.getProperties().getProperty("RACES").split(":", -1);
//
//        final Path[] race_paths = new Path[race_strings.length];
//
//        for (int i = 0; i < race_paths.length; i++)
//            race_paths[i] = Paths.get(race_strings[i]);
//
//        return race_paths;
//    }

    public IndividualRace[] loadSeriesRaces() throws IOException {

        final IndividualRace[] races = new IndividualRace[race_config_paths.length];

        for (int i = 0; i < races.length; i++) {

            final Path relative_path = race_config_paths[i];

            if (!relative_path.toString().isEmpty()) {

                final Path individual_race_path = race.getWorkingDirectoryPath().resolve(relative_path);

                IndividualRace individual_race = new IndividualRace(individual_race_path);
                individual_race.configure();
                individual_race.processResults(false);

                races[i] = individual_race;
            }
        }

        return races;
    }
}
