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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.time.Duration;
import java.util.Comparator;

public class LegResult extends RaceResult {

    public final RelayRaceEntry entry;
    int leg_number;
    boolean dnf;
    boolean in_mass_start;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    //////////////////////////////////////////////////////////////////////////////////////////////////

    LegResult(final RelayRaceEntry entry, final Race race) {

        super(race);

        this.entry = entry;
        dnf = true;
        in_mass_start = false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {
        return !dnf ? finish_time.minus(start_time) : null;
    }

    @Override
    public EntryCategory getCategory() {
        return entry.participant.category;
    }

    @Override
    protected String getIndividualRunnerName() {
        return ((Team)entry.participant).runner_names.get(leg_number - 1);
    }

    @Override
    public Participant getParticipant() {
        return entry.participant;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((LegResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public boolean canComplete() {
        return !dnf;
    }

    @Override
    public boolean shouldDisplayPosition() {
        return true;
    }
}
