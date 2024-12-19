/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race.tour;

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceInput;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.Race.*;

public class TourRaceInput extends SeriesRaceInput {

    private record SelfTimedRun(int bib_number, int race_number) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private List<Duration> wave_start_offsets;
    private List<SelfTimedRun> self_timed_runs;
    private List<EntryCategory> second_wave_categories;

    private int time_trial_race_number;
    private int time_trial_runners_per_wave;
    private Duration time_trial_inter_wave_interval;
    private Map<Integer, Duration> time_trial_starts;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    TourRaceInput(final Race race) {
        super(race);
    }

    @Override
    public void readProperties() {

        super.readProperties();

        wave_start_offsets = readWaveStartOffsets();
        self_timed_runs = readSelfTimedRuns();
        second_wave_categories = readSecondWaveCategories();

        readTimeTrialProperties();
    }

    @Override
    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) {

        applyRunnerStartOffsets(individual_race, race_number);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private List<Duration> readWaveStartOffsets() {

        final String[] offset_strings = race.getProperty(KEY_WAVE_START_OFFSETS, "").split(",", -1);

        return extractConfigFromPropertyStrings(offset_strings, Normalisation::parseTime);
    }

    private List<SelfTimedRun> readSelfTimedRuns() {

        final String[] self_timed_strings = race.getProperty(KEY_SELF_TIMED, "").split(",", -1);

        final Function<String, SelfTimedRun> extract_run_function = s -> {

            final String[] parts = s.split("/", -1);
            return new SelfTimedRun(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        };

        return extractConfigFromPropertyStrings(self_timed_strings, extract_run_function);
    }

    private List<EntryCategory> readSecondWaveCategories() {

        final String second_wave_categories_string = race.getProperty(KEY_SECOND_WAVE_CATEGORIES);
        if (second_wave_categories_string == null) return List.of();

        final String[] second_wave_category_strings = second_wave_categories_string.split(",", -1);

        return extractConfigFromPropertyStrings(second_wave_category_strings, race::lookupEntryCategory);
    }

    @SuppressWarnings("ZeroLengthArrayAllocation")
    private static <T> List<T> extractConfigFromPropertyStrings(final String[] strings, final Function<? super String, T> mapper) {

        final String[] non_empty_strings = strings.length == 1 && strings[0].isEmpty() ? new String[0] : strings;

        return Arrays.stream(non_empty_strings).map(mapper).toList();
    }

    private void readTimeTrialProperties() {

        time_trial_race_number = Integer.parseInt(race.getProperty(KEY_TIME_TRIAL_RACE));

        final String[] parts = race.getProperty(KEY_TIME_TRIAL_STARTS).split(",", -1);

        if (parts.length == 2) {
            time_trial_runners_per_wave = Integer.parseInt(parts[0]);
            time_trial_inter_wave_interval = parseTime(parts[1]);

        } else
            time_trial_starts = loadTimeTrialStarts(parts);
    }

    private static Map<Integer, Duration> loadTimeTrialStarts(final String[] parts) {

        final Map<Integer, Duration> starts = new HashMap<>();
        for (final String part : parts) {
            final String[] split = part.split("/");
            starts.put(Integer.parseInt(split[0]), parseTime(split[1]));
        }
        return starts;
    }

    private void applyRunnerStartOffsets(final IndividualRace individual_race, final int race_number) {

        for (final RawResult result : individual_race.getRawResults()) {

            final Duration runner_start_offset = getRunnerStartOffset(individual_race, race_number, result.getBibNumber());
            result.recorded_finish_time = result.recorded_finish_time.minus(runner_start_offset);
        }
    }

    private Duration getRunnerStartOffset(final IndividualRace individual_race, final int race_number, final int bib_number) {

        if (isRunnerSelfTimed(race_number, bib_number))
            return Duration.ZERO;

        if (isRaceTimeTrial(race_number))
            return getTimeTrialOffset(bib_number);

        if (isRunnerInSecondWave(individual_race, bib_number))
            return wave_start_offsets.get(race_number - 1);

        return Duration.ZERO;
    }

    private Duration getTimeTrialOffset(final int bib_number) {

        // The first option applies when time-trial runners are assigned to waves in order of bib number,
        // with incomplete waves if there are any gaps in bib numbers.
        // The second option applies when start order is manually determined (e.g. to start current leaders first or last).

        if (time_trial_starts == null) {

            final int wave_number = runnerIndexInBibOrder(bib_number) / time_trial_runners_per_wave;
            return time_trial_inter_wave_interval.multipliedBy(wave_number);

        } else
            return time_trial_starts.get(bib_number);
    }

    private static int runnerIndexInBibOrder(final int bib_number) {
        return bib_number - 1;
    }

    private boolean isRaceTimeTrial(final int race_number) {
        return race_number == time_trial_race_number;
    }

    private boolean isRunnerSelfTimed(final int race_number, final int bib_number) {

        return self_timed_runs.stream().anyMatch(self_timed_run -> self_timed_run.race_number == race_number && self_timed_run.bib_number == bib_number);
    }

    private boolean isRunnerInSecondWave(final IndividualRace individual_race, final int bib_number) {

        final EntryCategory runner_entry_category = individual_race.findCategory(bib_number);

        return second_wave_categories.stream().
            map(second_wave_category -> second_wave_category.equals(runner_entry_category)).
            reduce(Boolean::logicalOr).
            orElse(false);
    }
}
