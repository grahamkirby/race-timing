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

    public RelayRaceResult(final RelayRaceEntry entry, final int number_of_legs, final RelayRace race) {

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
        return duration().compareTo(((RelayRaceResult) other).duration());
    }

    @Override
    public boolean completed() {
        return !dnf();
    }

    @Override
    public EntryCategory getCategory() {
        return entry.team.category();
    }

    @Override
    public boolean shouldDisplayPosition() {
        return completed();
    }

    public Duration duration() {

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

    protected boolean allLegsDnf() {

        for (final LegResult leg_result : leg_results)
            if (!leg_result.DNF) return false;

        return true;
    }
}
