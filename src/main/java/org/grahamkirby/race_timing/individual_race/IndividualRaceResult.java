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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.Comparator;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

public class IndividualRaceResult extends SingleRaceResult {

    public IndividualRaceResult(final RaceEntry entry, final Duration finish_time, final RaceInternal race) {

        super(race, entry, finish_time);
    }

    @Override
    public Comparator<RaceResult> getComparator() {

        return consecutiveComparator(
            CommonRaceResult::comparePossibleCompletion,

            conditionalComparator(
                either_is_DNF,

                consecutiveComparator(
                    CommonRaceResult::compareRunnerLastName,
                    CommonRaceResult::compareRunnerFirstName
                ),

                consecutiveComparator(
                    CommonRaceResult::comparePerformance,
                    SingleRaceResult::compareRecordedPosition,
                    CommonRaceResult::compareRunnerLastName,
                    CommonRaceResult::compareRunnerFirstName
                )
            )
        );
    }

    @Override
    public String getPrizeDetail() {

        return "(" + ((Runner) getParticipant()).getClub() + ") " + renderDuration(this, DNF_STRING);
    }

    final RaceResultComparatorPredicate<RaceResult> either_is_DNF =
        (RaceResult result1, RaceResult result2) -> !result1.canComplete() || !result2.canComplete();
}
