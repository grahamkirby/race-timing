package series_race;

import common.Race;
import common.RaceInput;
import individual_race.IndividualRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SeriesRaceInput extends RaceInput {

    public List<Path> race_config_paths;

    public SeriesRaceInput(final Race race) {

        super(race);
        readProperties();
    }

    public List<IndividualRace> loadRaces() throws IOException {

        final List<IndividualRace> races = new ArrayList<>();

        for (int i = 0; i < race_config_paths.size(); i++) {

            final Path relative_path = race_config_paths.get(i);

            if (!relative_path.toString().isEmpty())
                races.add(getIndividualRace(relative_path, i + 1));
            else
                races.add(null);
        }

        return races;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        race_config_paths = readRaceConfigPaths();
    }

    private List<Path> readRaceConfigPaths() {

        final String[] race_strings = race.getProperties().getProperty("RACES").split(",", -1);

        final List<Path> race_paths = new ArrayList<>();

        for (final String race_string : race_strings)
            race_paths.add(Paths.get(race_string));

        return race_paths;
    }

    protected IndividualRace getIndividualRace(final Path relative_path, final int race_number) throws IOException {

        final Path individual_race_path = race.getWorkingDirectoryPath().resolve(relative_path);
        final IndividualRace individual_race = new IndividualRace(individual_race_path);

        configureIndividualRace(individual_race, race_number);
        individual_race.processResults(false);

        return individual_race;
    }

    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) throws IOException {

    }
}
