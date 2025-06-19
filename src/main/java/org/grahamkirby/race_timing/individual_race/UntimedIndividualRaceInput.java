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

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_RESULTS_PATH;

class UntimedIndividualRaceInput extends SingleRaceInput {

    private String overall_results_path;

    private int next_fake_bib_number = 1;

    private final Function<String, RaceResult> race_result_mapper = line -> makeRaceResult(new ArrayList<>(Arrays.stream(line.split("\t")).toList()));

    UntimedIndividualRaceInput(final UntimedIndividualRace race) {
        super(race);
    }

    @Override
    protected void readProperties() {

        overall_results_path = race.getOptionalProperty(KEY_RESULTS_PATH);
    }

    @Override
    protected IndividualRaceEntry makeRaceEntry(final List<String> elements) {
        return null;
    }

//    @Override
//    protected void validateConfig() {
//    }

    @Override
    protected void validateEntries() {
    }

    List<RaceResult> loadOverallResults() throws IOException {

        return Files.readAllLines(race.getPath(overall_results_path)).stream().
            map(SingleRaceInput::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(race_result_mapper).
            filter(Objects::nonNull).
            toList();
    }

    private RaceResult makeRaceResult(final List<String> elements) {

        elements.addFirst(String.valueOf(next_fake_bib_number++));

        final SingleRaceEntry entry = new IndividualRaceEntry(elements, race);
        final Duration finish_time = Normalisation.parseTime(elements.getLast());

        return new SingleRaceResult(race, entry, finish_time);
    }
}
