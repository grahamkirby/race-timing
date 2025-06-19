/*
 * Copyright 2025 Graham Kirby:
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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.relay_race.RelayRace;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;

public class TimedIndividualRace extends TimedRace {

    private static final String KEY_INDIVIDUAL_EARLY_STARTS = "INDIVIDUAL_EARLY_STARTS";

    public TimedIndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
    public static void main(final String[] args) throws Exception {

        commonMain(args, config_file_path -> new TimedIndividualRace(Paths.get(config_file_path)));
    }

    @Override
    protected RaceInput getInput() {
        return new TimedIndividualRaceInput(this);
    }

    @Override
    public void calculateResults() {

        initialiseResults();
        configureIndividualEarlyStarts();
        super.calculateResults();
    }

    private void initialiseResults() {

        overall_results = raw_results.stream().
            map(this::makeResult).
            toList();

        overall_results = makeMutable(overall_results);
    }

    private RaceResult makeResult(final RawResult raw_result) {

        final int bib_number = raw_result.getBibNumber();
        final Duration finish_time = raw_result.getRecordedFinishTime();

        return new SingleRaceResult(this, getEntryWithBibNumber(bib_number), finish_time);
    }

    private void configureIndividualEarlyStarts() {

        final String individual_early_starts_string = getOptionalProperty(KEY_INDIVIDUAL_EARLY_STARTS);

        // bib number / start time difference
        // Example: INDIVIDUAL_EARLY_STARTS = 2/0:10:00,26/0:20:00

        if (individual_early_starts_string != null)
            Arrays.stream(individual_early_starts_string.split(",")).
                forEach(this::recordEarlyStart);
    }

    private void recordEarlyStart(final String early_starts_string) {

        final String[] split = early_starts_string.split("/");

        final int bib_number = Integer.parseInt(split[0]);
        final Duration offset = parseTime(split[1]);

        final SingleRaceResult result = getResultByBibNumber(bib_number);

        result.finish_time = result.finish_time.plus(offset);
    }

    private SingleRaceResult getResultByBibNumber(final int bib_number) {

        return (SingleRaceResult) overall_results.stream().
            filter(result -> ((SingleRaceResult) result).entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }
}
