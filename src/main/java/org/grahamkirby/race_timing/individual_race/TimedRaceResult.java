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

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.time.Duration;
import java.util.Comparator;

public abstract class TimedRaceResult extends SingleRaceResult {

//    public final TimedRaceEntry entry;
//    Duration finish_time;
    CompletionStatus completion_status;  // TODO why only stored for individual race?

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public TimedRaceResult(final TimedRace race, final TimedRaceEntry entry, final Duration finish_time) {

        super(race, entry, finish_time);
//        this.entry = entry;

        // Provisionally this result is COMPLETED since a finish time was recorded.
        // However, it might still be set to DNF in recordDNF() if the runner didn't complete the course.
        completion_status = CompletionStatus.COMPLETED;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public CompletionStatus getCompletionStatus() {
        return completion_status;
    }

    @Override
    public EntryCategory getCategory() {
        return entry.participant.category;
    }

    @Override
    protected String getIndividualRunnerName() {
        return entry == null ? null : entry.participant.name;
    }

//    @Override
//    protected String getIndividualRunnerClub() {
//        return entry == null ? null : ((Runner)entry.participant).club;
//    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((TimedRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public boolean shouldDisplayPosition() {
        return completion_status == CompletionStatus.COMPLETED;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {
        return finish_time;
    }
}
