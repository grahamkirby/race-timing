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
            CommonRaceResult::comparePossibleCompletion,

            conditionalComparator(
                both_completed,

                consecutiveComparator(
                    CommonRaceResult::comparePerformance,
                    conditionalComparator(
                        neither_dead_heat,
                        SingleRaceResult::compareRecordedPosition,
                        name_comparator
                    )
                )
            ),

            name_comparator
        );
    }

    final Comparator<RaceResult> name_comparator =
        consecutiveComparator(
            CommonRaceResult::compareRunnerLastName,
            CommonRaceResult::compareRunnerFirstName
        );

    final ComparatorPredicate<RaceResult> both_completed =
        (RaceResult result1, RaceResult result2) -> result1.canOrHasCompleted() && result2.canOrHasCompleted();

    final ComparatorPredicate<RaceResult> neither_dead_heat =
        (RaceResult result1, RaceResult result2) -> {

            final RaceResultsProcessor processor = ((IndividualRaceResult) result1).race.getResultsProcessor();

            return processor.canDistinguishFromOtherEqualPerformances(result1) &&
                processor.canDistinguishFromOtherEqualPerformances(result2);
        };

    @Override
    public String toString() {

        return getParticipant() + " " + renderDuration(this, DNF_STRING);
    }
}
