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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Race2;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsCalculator;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

public class GrandPrixRaceResultsCalculator extends SeriesRaceResultsCalculator {

    // TODO check 2016 results re standings after 10 races, and senior categories shouldn't include vet.
    protected RaceResult getOverallResult(final Runner runner) {

        final List<Integer> scores = ((GrandPrixRace) race.getSpecific()).getRaces().stream().
            map(individual_race -> calculateRaceScore(individual_race, runner)).
            toList();

        return new GrandPrixRaceResult(runner, scores, race);
    }

    protected Predicate<RaceResult> getResultInclusionPredicate() {

        return result -> ((GrandPrixRace) race.getSpecific()).qualifying_clubs.contains(((Runner) result.getParticipant()).getClub());
    }

    int calculateRaceScore(final Race2 individual_race, final Runner runner) {

        if (individual_race == null) return 0;

        final Duration runner_time = getRunnerTime(individual_race, runner);

        return runner_time == null ? 0 : (int) Math.round(divide(runner_time, ((IndividualRaceResultsCalculator) individual_race.getResultsCalculator()).getMedianTime()) * ((GrandPrixRace) race.getSpecific()).score_for_median_position);
    }

    private static double divide(final Duration d1, final Duration d2) {

        return d1.toMillis() / (double) d2.toMillis();
    }

    int getRaceNumberInTemporalPosition(final int position) {
        return ((GrandPrixRace) race.getSpecific()).race_temporal_positions.get(position) - 1;
    }
}
