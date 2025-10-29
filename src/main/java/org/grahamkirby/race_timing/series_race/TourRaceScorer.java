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
import org.grahamkirby.race_timing.individual_race.Runner;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;

public class TourRaceScorer implements SeriesRaceScorer {

    private final RaceInternal race;
    final BiFunction<SingleRaceInternal, Runner, Duration> get_runner_time;

    public TourRaceScorer(final RaceInternal race, final BiFunction<SingleRaceInternal, Runner, Duration> get_runner_time) {

        this.race = race;
        this.get_runner_time = get_runner_time;
    }

    @Override
    public RaceResult makeOverallResult(final Runner runner, final List<Object> scores) {

        return new TourRaceResult(runner, scores, race);
    }

    @Override
    public Object calculateRaceScore(final Runner runner, final SingleRaceInternal individual_race) {

        if (individual_race == null) return null;

        return get_runner_time.apply(individual_race, runner);
    }
}
