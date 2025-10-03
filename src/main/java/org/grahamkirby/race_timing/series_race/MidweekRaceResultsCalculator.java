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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.SingleRaceResult;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.List;

import static org.grahamkirby.race_timing.common.Config.KEY_SCORE_FOR_FIRST_PLACE;

public class MidweekRaceResultsCalculator extends SeriesRaceResultsCalculator {

    int calculateRaceScore(final Race individual_race, final Runner runner) {

        if (individual_race == null) return 0;

        // The first finisher of each gender gets the maximum score, the next finisher one less, and so on.

        final List<SingleRaceResult> gender_results = individual_race.getResultsCalculator().getOverallResults().stream().
            map(result -> (SingleRaceResult) result).
            filter(SingleRaceResult::canComplete).
            filter(result -> result.getCategory().getGender().equals(runner.category.getGender())).
            toList();

        final int gender_position = (int) gender_results.stream().
            takeWhile(result -> !result.getParticipant().equals(runner)).
            count() + 1;

        return gender_position <= gender_results.size() ? Math.max((int) race.getConfig().get(KEY_SCORE_FOR_FIRST_PLACE) - gender_position + 1, 0) : 0;
    }

    protected RaceResult getOverallResult(final Runner runner) {

        final List<Integer> scores = ((SeriesRace) race.getSpecific()).getRaces().stream().
            map(individual_race -> calculateRaceScore(individual_race, runner)).
            toList();

        return new MidweekRaceResult(runner, scores, race);
    }
}
