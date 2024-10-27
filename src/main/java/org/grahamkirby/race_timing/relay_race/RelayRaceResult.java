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
import java.util.ArrayList;
import java.util.List;

public class RelayRaceResult extends RaceResult {

    public final RelayRaceEntry entry;
    final List<LegResult> leg_results;

    public RelayRaceResult(final RelayRaceEntry entry, final int number_of_legs, final Race race) {

        super(race);
        this.entry = entry;
        leg_results = new ArrayList<>();

        for (int i = 0; i < number_of_legs; i++)
            leg_results.add(new LegResult(entry, race));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(final RaceResult other) {
        return compare(this, other);
    }

    @Override
    public boolean sameEntrant(final RaceResult other) {
        return entry.equals(((RelayRaceResult) other).entry);
    }

    @Override
    public boolean completed() {
        return !dnf();
    }

    @Override
    public EntryCategory getCategory() {
        return entry.team.category();
    }

    protected Duration duration() {

        Duration overall = Duration.ZERO;

        for (final LegResult leg_result : leg_results) {

            if (leg_result.DNF) return Race.DUMMY_DURATION;
            overall = overall.plus(leg_result.duration());
        }

        return overall;
    }

    protected boolean dnf() {

        for (final LegResult leg_result : leg_results)
            if (leg_result.DNF) return true;

        return false;
    }

    protected static int compare(final RaceResult r1, final RaceResult r2) {

        final RelayRaceResult result1 = (RelayRaceResult) r1;
        final RelayRaceResult result2 = (RelayRaceResult) r2;

        final int completion_comparison = compareCompletion(!result1.dnf(), !result2.dnf());
        final int bib_comparison = Integer.compare(result1.entry.bib_number, result2.entry.bib_number);
        final int duration_comparison = result1.duration().compareTo(result2.duration());
        final int last_leg_position_comparison = Integer.compare(getRecordedLastLegPosition(result1), getRecordedLastLegPosition(result2));

        if (completion_comparison != 0) return completion_comparison;   // If one has completed and the other has not, order by completion first.
        if (result1.dnf()) return bib_comparison;                       // If the first has not completed (then implicitly neither has the second), order by bib number.
        if (duration_comparison != 0) return duration_comparison;       // If the durations are different, order by duration.

        return last_leg_position_comparison;                            // Both completed, with same overall duration, order by last leg finish position.
    }

    private static int compareCompletion(final boolean completed1, final boolean completed2) {

        if (completed1 && !completed2) return -1;
        if (!completed1 && completed2) return 1;
        return 0;
    }

    private static int getRecordedLastLegPosition(final RelayRaceResult result) {

        final RelayRace race = (RelayRace) result.race;
        return race.getRecordedLegPosition(result.entry.bib_number, race.number_of_legs);
    }
}
