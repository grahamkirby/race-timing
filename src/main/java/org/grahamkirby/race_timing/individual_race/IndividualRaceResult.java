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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.time.Duration;
import java.util.Comparator;

public class IndividualRaceResult extends RaceResult {

    public final IndividualRaceEntry entry;
    public Duration finish_time;
    public CompletionStatus completion_status;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRaceResult(final IndividualRace race, final IndividualRaceEntry entry) {

        super(race);
        this.entry = entry;

        // Initialised in IndividualRace.fillFinishTimes().
        finish_time = null;

        // Will be changed later if a time is processed for this runner.
        completion_status = CompletionStatus.DNS;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public CompletionStatus getCompletionStatus() {
        return completion_status;
    }

    @Override
    public EntryCategory getCategory() {
        return entry.runner.category;
    }

    @Override
    protected String getIndividualRunnerName() {
        return entry == null ? null : entry.runner.name;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((IndividualRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public boolean shouldDisplayPosition() {
        return completion_status == CompletionStatus.COMPLETED;
    }

    @Override
    public boolean shouldBeDisplayedInResults() {
        return completion_status != CompletionStatus.DNS;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {
        return finish_time;
    }
}
