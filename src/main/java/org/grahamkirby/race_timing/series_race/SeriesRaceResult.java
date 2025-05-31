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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.util.Objects;

public abstract class SeriesRaceResult extends RaceResult {

    // This refers directly to a runner rather than to an intermediate entry object as in
    // SingleRaceResult, because in a series race the runner enters (and receives a bib
    // number for) the individual component races, not the overall series.

    public final Runner runner;

    protected SeriesRaceResult(final Runner runner, final Race race) {

        super(race);
        this.runner = runner;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EntryCategory getCategory() {
        return runner.category;
    }

    @Override
    public boolean canComplete() {

        final SeriesRace series_race = (SeriesRace) race;
        final int number_of_races_remaining = series_race.getNumberOfRacesInSeries() - series_race.getNumberOfRacesTakenPlace();

        return numberOfRacesCompleted() + number_of_races_remaining >= series_race.getMinimumNumberOfRaces();
    }

    @Override
    public boolean shouldDisplayPosition() {

        return canComplete();
    }

    @Override
    protected String getParticipantName() {
        return runner.name;
    }

    @Override
    public Participant getParticipant() {
        return runner;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean hasCompletedSeries() {

        return numberOfRacesCompleted() >= ((SeriesRace) race).getMinimumNumberOfRaces();
    }

    protected int numberOfRacesCompleted() {

        return (int) ((SeriesRace) race).races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            filter(result -> result.entry.participant.equals(runner)).
            filter(SingleRaceResult::canComplete).
            count();
    }
}
