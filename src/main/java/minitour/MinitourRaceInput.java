package minitour;

import common.Category;
import common.RawResult;
import individual_race.IndividualRace;
import individual_race.IndividualRaceEntry;
import individual_race.IndividualRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class MinitourRaceInput {

    final MinitourRace race;

    Path[] race_config_paths;


    public MinitourRaceInput(final MinitourRace race) {

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

        final String[] race_strings = race.getProperties().getProperty("RACES").split(":", -1);

        final Path[] race_paths = new Path[race_strings.length];

        for (int i = 0; i < race_paths.length; i++)
            race_paths[i] = Paths.get(race_strings[i]);

        return race_paths;
    }

    private List<String> second_wave_category_names = Arrays.asList("FU9", "MU9", "FU11", "MU11");

    public IndividualRace[] loadMinitourRaces() throws IOException {

        final IndividualRace[] races = new IndividualRace[race_config_paths.length];


        for (int i = 0; i < races.length; i++) {

            final Path relative_path = race_config_paths[i];

            if (!relative_path.toString().isEmpty()) {

                final Path individual_race_path = race.getWorkingDirectoryPath().resolve(relative_path);

                IndividualRace individual_race = new IndividualRace(individual_race_path);
                individual_race.configure();

                for (RawResult raw_result : individual_race.getRawResults()) {

                    int bibNumber = raw_result.getBibNumber();

                    Duration start_offset = getStartOffset(individual_race, i+1, bibNumber);

                    raw_result.recorded_finish_time = raw_result.recorded_finish_time.minus(start_offset);
                }

                individual_race.processResults(false);

                races[i] = individual_race;
            }
        }

        return races;
    }

    private String[] start_offsets = {"00:00:00", "00:01:45", "00:01:00", "00:00:00", "00:02:00"};

    private boolean selfTimed(int race_number, int bib_number) {

        return race_number == 4 && bib_number == 23;
    }

    private Duration getStartOffset(IndividualRace individual_race, int race_number, int bib_number) {

        if (selfTimed(race_number, bib_number)) return Duration.ZERO;

        if (race_number == 4) return IndividualRace.parseTime("00:00:30").multipliedBy((bib_number-1)/4);

        Category category = null;
        for (IndividualRaceEntry entry2 : individual_race.entries) {
            if (entry2.bib_number == bib_number) {
                category = entry2.runner.category;
            }
        }


        if (second_wave_category_names.contains(category.getShortName())) {
            return IndividualRace.parseTime(start_offsets[race_number-1]);
        }

        return IndividualRace.ZERO_TIME;
    }
}
