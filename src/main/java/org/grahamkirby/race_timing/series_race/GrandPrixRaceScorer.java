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
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsCalculator;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;

import static org.grahamkirby.race_timing.common.Config.KEY_SCORE_FOR_MEDIAN_POSITION;

public class GrandPrixRaceScorer implements SeriesRaceScorer {

    private final int score_for_median_position;
    private final RaceInternal race;
    final BiFunction<SingleRaceInternal, Runner, Duration> get_runner_time;

    public GrandPrixRaceScorer(final RaceInternal race, final BiFunction<SingleRaceInternal, Runner, Duration> get_runner_time) {

        this.race = race;
        score_for_median_position = (int) race.getConfig().get(KEY_SCORE_FOR_MEDIAN_POSITION);
        this.get_runner_time = get_runner_time;
    }

    // TODO check 2016 results re standings after 10 races, and senior categories shouldn't include vet.
    public RaceResult getOverallResult(final Runner runner) {

        final List<Object> scores = ((SeriesRace) race).getRaces().stream().
            map(individual_race -> calculateRaceScore(individual_race, runner)).
            toList();

        return new GrandPrixRaceResult(runner, scores, race);
    }

    public Object calculateRaceScore(final SingleRaceInternal individual_race, final Runner runner) {

        final Duration runner_time = get_runner_time.apply(individual_race, runner);

        // Runner may not have competed in this race.
        if (runner_time != null) {

            final Duration median_time = ((IndividualRaceResultsCalculator) individual_race.getResultsCalculator()).getMedianTime();
            final double time_ratio = runner_time.toMillis() / (double) median_time.toMillis();

            // Lower score is better.
            return (int) Math.round(time_ratio * score_for_median_position);

        } else {
            return null;
        }
    }
}
