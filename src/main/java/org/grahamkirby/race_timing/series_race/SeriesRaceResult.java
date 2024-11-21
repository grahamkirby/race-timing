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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;

import java.util.Objects;

public abstract class SeriesRaceResult extends RaceResult {

    // This refers directly to a runner rather than to an intermediate entry object as in
    // IndividualRaceResult and RelayRaceResult, because in a series race the runner enters
    // (and receives a bib number for) the individual component races, not the overall series.
    public final Runner runner;

    public SeriesRaceResult(final Runner runner, final Race race) {

        super(race);
        this.runner = runner;
    }

    @Override
    public boolean completed() {
        return numberCompleted() >= ((SeriesRace)race).minimum_number_of_races;
    }

    @Override
    public EntryCategory getCategory() {
        return runner.category;
    }

    @Override
    public boolean sameEntrant(final RaceResult other) {
        return runner.equals(((SeriesRaceResult) other).runner);
    }

    private int numberCompleted() {

        return (int) ((SeriesRace)race).races.stream().
                filter(Objects::nonNull).
                flatMap(race -> race.getOverallResults().stream()).
                map(result -> (IndividualRaceResult)result).
                filter(result -> result.entry.runner.equals(runner) && result.completed()).
                count();
    }
}
