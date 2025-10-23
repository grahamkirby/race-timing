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


import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.VERY_LONG_DURATION;

public class RelayRaceLegResult extends SingleRaceResult {

    private int leg_number;
    private boolean in_mass_start;

    // TODO move to SingleRaceResult, integrate with early starts.
    Duration start_time;  // Relative to start of leg 1.

    //////////////////////////////////////////////////////////////////////////////////////////////////

    RelayRaceLegResult(final RaceInternal race, final RaceEntry entry) {

        super(race, entry, null);

        setDnf(true);
        in_mass_start = false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Duration duration() {

        return canComplete() ?  finish_time.minus(start_time) : VERY_LONG_DURATION;
    }

    @Override
    public String getParticipantName() {

        return ((Team) getParticipant()).getRunnerNames().get(leg_number - 1);
    }

    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return leg_number == 1 ?
            List.of(
                CommonRaceResult::comparePossibleCompletion,
                CommonRaceResult::comparePerformance,
                this::compareRecordedPosition) :
            List.of(
                CommonRaceResult::comparePossibleCompletion,
                CommonRaceResult::comparePerformance,
                CommonRaceResult::compareRunnerLastName,
                CommonRaceResult::compareRunnerFirstName);
    }

    @Override
    public String getPrizeDetail() {

        return null;
    }

    public boolean isInMassStart() {
        return in_mass_start;
    }

    public void setInMassStart(final boolean in_mass_start) {
        this.in_mass_start = in_mass_start;
    }

    public int getLegNumber() {
        return leg_number;
    }

    public void setLegNumber(final int leg_number) {
        this.leg_number = leg_number;
    }
}
