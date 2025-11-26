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

import java.util.Comparator;

public class RelayRaceLegResult extends SingleRaceResult {

    private int leg_number;
    private boolean in_mass_start;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    RelayRaceLegResult(final RaceInternal race, final RaceEntry entry) {

        super(race, entry, null);

        setDnf(true);
        in_mass_start = false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getParticipantName() {

        return ((Team) getParticipant()).getRunnerNames().get(leg_number - 1);
    }

    @Override
    public Comparator<RaceResult> getComparator() {

        final RaceResultComparatorPredicate<RaceResult> first_leg =
            (RaceResult _, RaceResult _) -> leg_number == 1;

        return conditionalComparator(
            first_leg,

            consecutiveComparator(
                CommonRaceResult::comparePossibleCompletion,
                CommonRaceResult::comparePerformance,
                SingleRaceResult::compareRecordedPosition),

            consecutiveComparator(
                CommonRaceResult::comparePossibleCompletion,
                CommonRaceResult::comparePerformance,
                CommonRaceResult::compareRunnerLastName,
                CommonRaceResult::compareRunnerFirstName)
        );
    }

    @Override
    public String getPrizeDetail() {

        throw new UnsupportedOperationException();
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
