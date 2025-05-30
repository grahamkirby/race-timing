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

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.individual_race.TimedRaceResult;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RelayRaceResult extends TimedRaceResult {

    final List<LegResult> leg_results;

    RelayRaceResult(final RelayRaceEntry entry, final RelayRace race) {

        super(race, entry, null);
        leg_results = new ArrayList<>();

        for (int i = 0; i < race.getNumberOfLegs(); i++)
            leg_results.add(new LegResult(entry, race));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getIndividualRunnerName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Participant getParticipant() {
        return entry.participant;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((SingleRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public boolean canComplete() {

        return leg_results.stream().allMatch(LegResult::canComplete);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {

        return !canComplete() ? null :
            leg_results.stream().
                map(LegResult::duration).
                reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    protected String getClub() {
        return null;
    }
}
