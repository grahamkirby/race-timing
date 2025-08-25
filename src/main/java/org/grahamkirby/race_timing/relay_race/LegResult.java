/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
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


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.time.Duration;

public class LegResult extends SingleRaceResult {

    int leg_number;
    boolean in_mass_start;

    Duration start_time;  // Relative to start of leg 1.

    //////////////////////////////////////////////////////////////////////////////////////////////////

    LegResult(final RelayRaceEntry entry, final Race race) {

        super(race, entry, null);

        dnf = true;
        in_mass_start = false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {
        return dnf ? null : finish_time.minus(start_time);
    }

    @Override
    protected String getParticipantName() {
        return ((Team) entry.participant).runner_names.get(leg_number - 1);
    }
}
