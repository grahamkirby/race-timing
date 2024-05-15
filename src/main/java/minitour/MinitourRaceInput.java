package minitour;

import common.Category;
import common.RawResult;
import individual_race.IndividualRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static common.Race.parseTime;

public class MinitourRaceInput {

    private record SelfTimedRun(int bib_number, int race_number) {}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    static private final List<String> SECOND_WAVE_CATEGORY_NAMES = Arrays.asList("FU9", "MU9", "FU11", "MU11");

    final MinitourRace race;

    Path[] race_config_paths;
    private String[] start_offsets;
    private SelfTimedRun[] self_timed_runs;

    private int time_trial_race_number;
    private int time_trial_runners_per_wave;
    private Duration time_trial_inter_wave_interval;

    public MinitourRaceInput(final MinitourRace race) {

        this.race = race;
        configure();
    }

    private void configure() {

        readProperties();
    }

    private void readProperties() {

        race_config_paths = readRaceConfigPaths();
        start_offsets = readStartOffsets();
        self_timed_runs = readSelfTimedRuns();

        readTimeTrialProperties();
    }

    private Path[] readRaceConfigPaths() {

        final String[] race_strings = race.getProperties().getProperty("RACES").split(",", -1);

        final Path[] race_paths = new Path[race_strings.length];

        for (int i = 0; i < race_paths.length; i++)
            race_paths[i] = Paths.get(race_strings[i]);

        return race_paths;
    }

    private String[] readStartOffsets() {

        return race.getProperties().getProperty("RACE_START_OFFSETS").split(",", -1);
    }

    private SelfTimedRun[] readSelfTimedRuns() {

        String[] selfTimeds = race.getProperties().getProperty("SELF_TIMED").split(",", -1);

        Stream<SelfTimedRun> selfTimedRunStream = Arrays.stream(selfTimeds).map(s -> {
            String[] parts = s.split("/", -1);
            return new SelfTimedRun(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        });

        return selfTimedRunStream.toList().toArray(new SelfTimedRun[0]);
    }

    private void readTimeTrialProperties() {

        String[] parts = race.getProperties().getProperty("TIME_TRIAL").split("/", -1);

        time_trial_race_number = Integer.parseInt(parts[0]);
        time_trial_runners_per_wave = Integer.parseInt(parts[1]);
        time_trial_inter_wave_interval = parseTime(parts[2]);
    }

    public IndividualRace[] loadMinitourRaces() throws IOException {

        final IndividualRace[] races = new IndividualRace[race_config_paths.length];

        for (int i = 0; i < races.length; i++) {

            final Path relative_path = race_config_paths[i];

            if (!relative_path.toString().isEmpty()) {

                final Path individual_race_path = race.getWorkingDirectoryPath().resolve(relative_path);

                IndividualRace individual_race = new IndividualRace(individual_race_path);
                individual_race.configure();

                for (RawResult raw_result : individual_race.getRawResults()) {

                    final Duration start_offset = getStartOffset(individual_race, i+1, raw_result.getBibNumber());

                    raw_result.recorded_finish_time = raw_result.recorded_finish_time.minus(start_offset);
                }

                individual_race.processResults(false);

                races[i] = individual_race;
            }
        }

        return races;
    }

    private boolean selfTimed(int bib_number, int race_number) {

        for (SelfTimedRun self_timed_run : self_timed_runs) {
            if (self_timed_run.bib_number == bib_number && self_timed_run.race_number == race_number) return true;
        }

        return false;
    }

    private Duration getStartOffset(IndividualRace individual_race, int race_number, int bib_number) {

        if (selfTimed(bib_number, race_number)) return Duration.ZERO;

        if (race_number == time_trial_race_number) return time_trial_inter_wave_interval.multipliedBy((bib_number-1)/time_trial_runners_per_wave);

        if (categoryInSecondWave(individual_race.findCategory(bib_number)))
            return parseTime(start_offsets[race_number-1]);

        return IndividualRace.ZERO_TIME;
    }

    private static boolean categoryInSecondWave(Category category) {
        return SECOND_WAVE_CATEGORY_NAMES.contains(category.getShortName());
    }
}
