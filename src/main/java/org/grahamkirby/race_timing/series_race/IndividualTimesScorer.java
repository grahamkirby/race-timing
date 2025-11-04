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

import org.grahamkirby.race_timing.common.Performance;
import org.grahamkirby.race_timing.common.SingleRaceInternal;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsCalculator;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.time.Duration;
import java.util.Objects;

public class IndividualTimesScorer extends SeriesRaceScorer {

    public IndividualTimesScorer(final SeriesRace race) {

        super(race);
    }

    // TODO check 2016 results re standings after 10 races, and senior categories shouldn't include vet.

    @Override
    public Performance getIndividualRacePerformance(final Runner runner, final SingleRaceInternal individual_race) {

        final Performance performance = getIndividualRaceTime(runner, individual_race);

        // Runner may not have competed in this race.
        if (performance == null) return null;

        final Duration runner_time = (Duration) performance.getValue();
        final Duration median_time = ((IndividualRaceResultsCalculator) individual_race.getResultsCalculator()).getMedianTime();
        final double time_ratio = runner_time.toMillis() / (double) median_time.toMillis();

        // Lower score is better.
        return new Performance((int) Math.round(time_ratio * score_for_median_position));
    }

    @Override
    public Performance getSeriesPerformance(final Runner runner) {

        final SeriesRaceResult series_result = ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getOverallResult(runner);
        final int number_of_counting_scores = Math.min(minimum_number_of_races, numberOfRacesCompleted(series_result));

        // Consider the lowest scores, since lower score is better.
        return new Performance(series_result.performances.stream().
            filter(Objects::nonNull).
            map(obj -> (int) obj.getValue()).
            sorted().
            limit(number_of_counting_scores).
            reduce(0, Integer::sum));
    }
}
