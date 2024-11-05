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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.time.Duration;

public class LegResult extends RaceResult {

    final RelayRaceEntry entry;
    int leg_number;
    boolean DNF;
    boolean in_mass_start;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    public LegResult(final RelayRaceEntry entry, final Race race) {

        super(race);
        this.entry = entry;
        this.DNF = true;
        this.in_mass_start = false;
    }

    public Duration duration() {
        return DNF ? Race.DUMMY_DURATION : finish_time.minus(start_time);
    }

    @Override
    public int compareTo(final RaceResult result) {

        final LegResult other = (LegResult) result;

        final int compare_duration = duration().compareTo(other.duration());
        if (compare_duration != 0) return compare_duration;

        // The times are the same, so use the recording order.
        final int this_recorded_position = ((RelayRace)race).getRecordedLegPosition(entry.bib_number, leg_number);
        final int other_recorded_position = ((RelayRace)race).getRecordedLegPosition(other.entry.bib_number, leg_number);

        return Integer.compare(this_recorded_position, other_recorded_position);
    }

    @Override
    public boolean sameEntrant(final RaceResult other) {
        return entry.equals(((LegResult) other).entry);
    }

    @Override
    public boolean completed() {
        return !DNF;
    }

    @Override
    public EntryCategory getCategory() {
        return entry.team.category();
    }

    @Override
    public int comparePerformanceTo(final RaceResult result) {

        final LegResult other = (LegResult) result;
        return duration().compareTo(other.duration());
    }

    @Override
    public boolean shouldDisplayPosition() {
        return true;
    }
}
