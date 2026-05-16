/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.Comparator;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.NormalisationProcessor.renderDuration;

public class IndividualRaceResult extends SingleRaceResult {

    public IndividualRaceResult(final RaceEntry entry, final Duration finish_time, final RaceInternal race) {

        super(race, entry, finish_time);
    }

    @Override
    public Comparator<RaceResult> getComparator() {

        // In individual race results, DNF results (completion not possible) appear after completed results.
        // Completed results are sorted by finish time, then for equal (but not explicitly recorded as dead heat)
        // times by recorded position, or for dead heats by runner name.
        //
        // DNF results are sorted by runner name.

        return consecutiveComparator(
            CommonRaceResult::comparePerformance,
            conditionalComparator(
                RaceResult::canDistinguishEqualPerformances,
                SingleRaceResult::compareRecordedPosition,
                consecutiveComparator(
                    CommonRaceResult::compareRunnerLastName,
                    CommonRaceResult::compareRunnerFirstName
                )
            )
        );
    }

    @Override
    public String toString() {

        return getParticipant() + " " + renderDuration(this, DNF_STRING);
    }

    @Override
    public boolean canDistinguishEqualPerformances(final RaceResult other) {

        // Normally results with same recorded time are not treated as dead heats, since an ordering is imposed at the
        // finish, but a dead heat can be recorded explicitly.
        return canOrHasCompleted() && !((IndividualRace) race).getDeadHeats().contains(getBibNumber());
    }
}
