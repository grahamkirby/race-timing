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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RelayRaceResult extends RaceResult {

    public final RelayRaceEntry entry;
    final List<LegResult> leg_results;

    public RelayRaceResult(final RelayRaceEntry entry, final RelayRace race) {

        super(race);
        this.entry = entry;
        leg_results = new ArrayList<>();

        for (int i = 0; i < race.number_of_legs; i++)
            leg_results.add(new LegResult(entry, race));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getIndividualRunnerName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((RelayRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public CompletionStatus getCompletionStatus() {

        if (!completedAnyLegs()) return CompletionStatus.DNS;
        if (!completedAllLegs()) return CompletionStatus.DNF;

        return CompletionStatus.COMPLETED;
    }

    @Override
    public EntryCategory getCategory() {
        return entry.team.category();
    }

    @Override
    public boolean shouldDisplayPosition() {
        return getCompletionStatus() == CompletionStatus.COMPLETED;
    }

    @Override
    public boolean shouldBeDisplayedInResults() {
        return getCompletionStatus() != CompletionStatus.DNS;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {

        return completedAllLegs() ?
            leg_results.stream().
            map(LegResult::duration).
            reduce(Duration::plus).
            orElse(Duration.ZERO) : null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean completedAllLegs() {

        return leg_results.stream().allMatch(result -> result.getCompletionStatus() == CompletionStatus.COMPLETED);
    }

    protected boolean completedAnyLegs() {

        return leg_results.stream().anyMatch(result -> result.getCompletionStatus() == CompletionStatus.COMPLETED);
    }
}
