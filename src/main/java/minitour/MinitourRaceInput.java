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

    private static final List<String> SECOND_WAVE_CATEGORY_NAMES = Arrays.asList("FU9", "MU9", "FU11", "MU11");

    private final MinitourRace race;

    private Path[] race_config_paths;
    private String[] wave_start_offsets;
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
        wave_start_offsets = readWaveStartOffsets();
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

    private String[] readWaveStartOffsets() {

        return race.getProperties().getProperty("WAVE_START_OFFSETS").split(",", -1);
    }

    private SelfTimedRun[] readSelfTimedRuns() {

        final String[] self_timed_strings = race.getProperties().getProperty("SELF_TIMED").split(",", -1);

        final Stream<SelfTimedRun> stream = Arrays.stream(self_timed_strings).map(s -> {
            final String[] parts = s.split("/", -1);
            return new SelfTimedRun(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        });

        return stream.toList().toArray(new SelfTimedRun[0]);
    }

    private void readTimeTrialProperties() {

        final String[] parts = race.getProperties().getProperty("TIME_TRIAL").split("/", -1);

        time_trial_race_number = Integer.parseInt(parts[0]);
        time_trial_runners_per_wave = Integer.parseInt(parts[1]);
        time_trial_inter_wave_interval = parseTime(parts[2]);
    }

    public IndividualRace[] loadMinitourRaces() throws IOException {

        final IndividualRace[] races = new IndividualRace[race_config_paths.length];

        for (int i = 0; i < races.length; i++) {

            final Path relative_path = race_config_paths[i];

            if (!relative_path.toString().isEmpty())
                races[i] = getIndividualRace(relative_path, i + 1);
        }

        return races;
    }

    private IndividualRace getIndividualRace(final Path relative_path, final int race_number) throws IOException {

        final Path individual_race_path = race.getWorkingDirectoryPath().resolve(relative_path);
        final IndividualRace individual_race = new IndividualRace(individual_race_path);

        for (final RawResult raw_result : individual_race.getRawResults()) {

            final Duration runner_start_offset = getStartOffset(individual_race, race_number, raw_result.getBibNumber());
            raw_result.recorded_finish_time = raw_result.recorded_finish_time.minus(runner_start_offset);
        }

        individual_race.processResults(false);

        return individual_race;
    }

    private Duration getStartOffset(final IndividualRace individual_race, final int race_number, final int bib_number) {

        if (selfTimed(race_number, bib_number))
            return Duration.ZERO;

        // This assumes that time-trial runners are assigned to waves in order of bib number.
        if (race_number == time_trial_race_number)
            return time_trial_inter_wave_interval.multipliedBy((bib_number-1)/time_trial_runners_per_wave);

        if (categoryInSecondWave(individual_race.findCategory(bib_number)))
            return parseTime(wave_start_offsets[race_number-1]);

        return Duration.ZERO;
    }

    private boolean selfTimed(final int race_number, final int bib_number) {

        for (SelfTimedRun self_timed_run : self_timed_runs)
            if (self_timed_run.bib_number == bib_number && self_timed_run.race_number == race_number) return true;

        return false;
    }

    private static boolean categoryInSecondWave(final Category category) {

        return SECOND_WAVE_CATEGORY_NAMES.contains(category.getShortName());
    }
}
