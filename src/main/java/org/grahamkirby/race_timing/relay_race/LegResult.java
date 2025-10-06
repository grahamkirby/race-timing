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
import java.util.concurrent.atomic.AtomicInteger;

import static org.grahamkirby.race_timing.common.Config.VERY_LONG_DURATION;

public class LegResult extends SingleRaceResult {

    int leg_number;
    boolean in_mass_start;

    // TODO move to SingleRaceResult, integrate with early starts.
    Duration start_time;  // Relative to start of leg 1.

    //////////////////////////////////////////////////////////////////////////////////////////////////

    LegResult(final Race race, final RaceEntry entry) {

        super(race, entry, null);
        dnf = true;
        in_mass_start = false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Duration duration() {

        return canComplete() ?  finish_time.minus(start_time) : VERY_LONG_DURATION;
    }

    @Override
    public String getParticipantName() {

        return ((Team) getParticipant()).runner_names.get(leg_number - 1);
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

    @Override
    public String getPrizeDetailText() {

        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected int getRecordedPosition(final int bib_number) {

        final AtomicInteger legs_completed = new AtomicInteger(0);

        return (int) race.getRaceData().getRawResults().stream().
            peek(result -> {
                if (result.getBibNumber() == bib_number) legs_completed.incrementAndGet();
            }).
            takeWhile(result -> result.getBibNumber() != bib_number || legs_completed.get() < leg_number).
            count() + 1;
    }
}
