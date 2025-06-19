/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.relay_race;


import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RelayRaceResult extends SingleRaceResult {

    final List<LegResult> leg_results;

    RelayRaceResult(final RelayRaceEntry entry, final RelayRace race) {

        super(race, entry, null);
        leg_results = new ArrayList<>();

        for (int i = 0; i < race.getNumberOfLegs(); i++)
            leg_results.add(new LegResult(entry, race));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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
