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

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.time.Duration;
import java.util.Comparator;

public abstract class TimedRaceResult extends SingleRaceResult {

    protected TimedRaceResult(final TimedRace race, final SingleRaceEntry entry, final Duration finish_time) {

        super(race, entry, finish_time);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EntryCategory getCategory() {
        return entry.participant.category;
    }

    @Override
    protected String getIndividualRunnerName() {
        return entry == null ? null : entry.participant.name;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((SingleRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public boolean shouldDisplayPosition() {
        return canComplete();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {
        return finish_time;
    }
}
