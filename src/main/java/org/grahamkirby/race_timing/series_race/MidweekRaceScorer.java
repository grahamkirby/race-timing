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

import org.grahamkirby.race_timing.common.RaceInternal;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.SingleRaceInternal;
import org.grahamkirby.race_timing.common.SingleRaceResult;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsCalculator;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;

import static org.grahamkirby.race_timing.common.Config.KEY_SCORE_FOR_FIRST_PLACE;
import static org.grahamkirby.race_timing.common.Config.KEY_SCORE_FOR_MEDIAN_POSITION;

public class MidweekRaceScorer implements SeriesRaceScorer {

    private final int score_for_first_place;
    private final RaceInternal race;
    final BiFunction<SingleRaceInternal, Runner, Duration> get_runner_time;

    public MidweekRaceScorer(final RaceInternal race, final BiFunction<SingleRaceInternal, Runner, Duration> get_runner_time) {

        this.race = race;
        score_for_first_place = (int) race.getConfig().get(KEY_SCORE_FOR_FIRST_PLACE);
        this.get_runner_time = get_runner_time;
    }

    public RaceResult getOverallResult(final Runner runner) {

        final List<Object> scores = ((SeriesRace) race).getRaces().stream().
            map(individual_race -> calculateRaceScore(individual_race, runner)).
            toList();

        return new MidweekRaceResult(runner, scores, race);
    }

    public Object calculateRaceScore(final SingleRaceInternal individual_race, final Runner runner) {

        if (individual_race == null) return 0;

        // The first finisher of each gender gets the maximum score, the next finisher one less, and so on.

        final List<SingleRaceResult> gender_results = individual_race.getResultsCalculator().getOverallResults().stream().
            map(result -> (SingleRaceResult) result).
            filter(SingleRaceResult::canComplete).
            filter(result -> result.getCategory().getGender().equals(runner.getCategory().getGender())).
            toList();

        final int gender_position = (int) gender_results.stream().
            takeWhile(result -> !result.getParticipant().equals(runner)).
            count() + 1;

        return gender_position <= gender_results.size() ? Math.max(score_for_first_place - gender_position + 1, 0) : 0;
    }
}
