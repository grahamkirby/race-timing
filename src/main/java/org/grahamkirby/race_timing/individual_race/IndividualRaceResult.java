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
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

public class IndividualRaceResult extends SingleRaceResult {

    public IndividualRaceResult(final RaceEntry entry, final Duration finish_time, final RaceInternal race) {

        super(race, entry, finish_time);
    }

    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return List.of(
            CommonRaceResult::comparePossibleCompletion,
            ignoreIfEitherResultIsDNF(CommonRaceResult::comparePerformance),
            ignoreIfEitherResultIsDNF(this::compareRecordedPosition),
            CommonRaceResult::compareRunnerLastName,
            CommonRaceResult::compareRunnerFirstName);
    }

    @Override
    public String getPrizeDetail() {

        return "(" + ((Runner) getParticipant()).getClub() + ") " + renderDuration(this, DNF_STRING);
    }

    private static Comparator<RaceResult> ignoreIfEitherResultIsDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() || !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }
}
