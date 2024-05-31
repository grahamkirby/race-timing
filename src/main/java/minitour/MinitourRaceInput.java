package minitour;

import common.Category;
import common.Race;
import common.RaceInput;
import common.RawResult;
import fife_ac_races.Minitour;
import individual_race.IndividualRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import static common.Race.parseTime;

public class MinitourRaceInput extends RaceInput {

    private record SelfTimedRun(int bib_number, int race_number) {}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final List<String> SECOND_WAVE_CATEGORY_NAMES = Arrays.asList("FU9", "MU9", "FU11", "MU11");

    private Duration[] wave_start_offsets;
    private SelfTimedRun[] self_timed_runs;

    private int time_trial_race_number;
    private int time_trial_runners_per_wave;
    private Duration time_trial_inter_wave_interval;

    public MinitourRaceInput(final Race race) {

        super(race);
    }

    @Override public void readProperties() {

        super.readProperties();
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

    private Duration[] readWaveStartOffsets() {

        final String[] offset_strings = race.getPropertyWithDefault("WAVE_START_OFFSETS", "").split(",", -1);

        return extractConfigFromPropertyStrings(offset_strings, Race::parseTime, Duration[]::new);
    }

    private SelfTimedRun[] readSelfTimedRuns() {

        final Function<String, SelfTimedRun> extract_run_function = s -> {

            final String[] parts = s.split("/", -1);
            return new SelfTimedRun(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        };

        final String[] self_timed_strings = race.getPropertyWithDefault("SELF_TIMED","").split(",", -1);

        return extractConfigFromPropertyStrings(self_timed_strings, extract_run_function, SelfTimedRun[]::new);
    }

    private <T> T[] extractConfigFromPropertyStrings(String[] strings, Function<String, T> mapper, IntFunction<T[]> array_element_initializer) {

        String[] non_empty_strings = strings.length == 1 && strings[0].isEmpty() ? new String[0] : strings;
        return Arrays.stream(non_empty_strings).map(mapper).toArray(array_element_initializer);
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

        applyRunnerStartOffsets(individual_race, race_number);
        individual_race.processResults(false);

        return individual_race;
    }

    private void applyRunnerStartOffsets(IndividualRace individual_race, int race_number) {

        for (final RawResult raw_result : individual_race.getRawResults()) {

            final Duration runner_start_offset = getRunnerStartOffset(individual_race, race_number, raw_result.getBibNumber());
            raw_result.recorded_finish_time = raw_result.recorded_finish_time.minus(runner_start_offset);
        }
    }

    private Duration getRunnerStartOffset(final IndividualRace individual_race, final int race_number, final int bib_number) {

        if (runnerIsSelfTimed(race_number, bib_number))
            return Duration.ZERO;

        if (raceIsTimeTrial(race_number))
            return getTimeTrialOffset(bib_number);

        if (runnerIsInSecondWave(individual_race, bib_number))
            return wave_start_offsets[race_number-1];

        return Duration.ZERO;
    }

    private Duration getTimeTrialOffset(int bib_number) {

        // This assumes that time-trial runners are assigned to waves in order of bib number, with incomplete waves if there are any gaps in bib numbers.

        final int wave_number = runnerIndexInBibOrder(bib_number) / time_trial_runners_per_wave;
        return time_trial_inter_wave_interval.multipliedBy(wave_number);
    }

    private int runnerIndexInBibOrder(int bib_number) {
        return bib_number - 1;
    }

    private boolean raceIsTimeTrial(int race_number) {
        return race_number == time_trial_race_number;
    }

    private boolean runnerIsSelfTimed(final int race_number, final int bib_number) {

        for (SelfTimedRun self_timed_run : self_timed_runs)
            if (self_timed_run.bib_number == bib_number && self_timed_run.race_number == race_number) return true;

        return false;
    }

    private boolean runnerIsInSecondWave(final IndividualRace individual_race, final int bib_number) {

        final Category category = individual_race.findCategory(bib_number);

        return SECOND_WAVE_CATEGORY_NAMES.contains(category.getShortName());
    }
}
