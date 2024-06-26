package fife_ac_races.minitour;

import common.Race;
import common.RawResult;
import individual_race.IndividualRace;
import series_race.SeriesRaceInput;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static common.Race.parseTime;

public class MinitourRaceInput extends SeriesRaceInput {

    private record SelfTimedRun(int bib_number, int race_number) {}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final List<String> SECOND_WAVE_CATEGORY_NAMES = Arrays.asList("FU9", "MU9", "FU11", "MU11");

    private List<Duration> wave_start_offsets;
    private List<SelfTimedRun> self_timed_runs;

    private int time_trial_race_number;
    private int time_trial_runners_per_wave;
    private Duration time_trial_inter_wave_interval;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MinitourRaceInput(final Race race) {
        super(race);
    }

    @Override
    public void readProperties() {

        super.readProperties();

        wave_start_offsets = readWaveStartOffsets();
        self_timed_runs = readSelfTimedRuns();

        readTimeTrialProperties();
    }

    @Override
    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) {

        applyRunnerStartOffsets(individual_race, race_number);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private List<Duration> readWaveStartOffsets() {

        final String[] offset_strings = race.getPropertyWithDefault("WAVE_START_OFFSETS", "").split(",", -1);

        return extractConfigFromPropertyStrings(offset_strings, Race::parseTime);
    }

    private List<SelfTimedRun> readSelfTimedRuns() {

        final String[] self_timed_strings = race.getPropertyWithDefault("SELF_TIMED","").split(",", -1);

        final Function<String, SelfTimedRun> extract_run_function = s -> {

            final String[] parts = s.split("/", -1);
            return new SelfTimedRun(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        };

        return extractConfigFromPropertyStrings(self_timed_strings, extract_run_function);
    }

    private <T> List<T> extractConfigFromPropertyStrings(final String[] strings, final Function<String, T> mapper) {

        final String[] non_empty_strings = strings.length == 1 && strings[0].isEmpty() ? new String[0] : strings;

        return Arrays.stream(non_empty_strings).map(mapper).toList();
    }

    private void readTimeTrialProperties() {

        final String[] parts = race.getProperties().getProperty("TIME_TRIAL").split("/", -1);

        time_trial_race_number = Integer.parseInt(parts[0]);
        time_trial_runners_per_wave = Integer.parseInt(parts[1]);
        time_trial_inter_wave_interval = parseTime(parts[2]);
    }

    private void applyRunnerStartOffsets(final IndividualRace individual_race, final int race_number) {

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
            return wave_start_offsets.get(race_number - 1);

        return Duration.ZERO;
    }

    private Duration getTimeTrialOffset(final int bib_number) {

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

        for (final SelfTimedRun self_timed_run : self_timed_runs)
            if (self_timed_run.bib_number == bib_number && self_timed_run.race_number == race_number) return true;

        return false;
    }

    private boolean runnerIsInSecondWave(final IndividualRace individual_race, final int bib_number) {

        return SECOND_WAVE_CATEGORY_NAMES.contains(individual_race.findCategory(bib_number).getShortName());
    }
}
