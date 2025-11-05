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
import org.grahamkirby.race_timing.common.SingleRaceResult;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.Comparator.reverseOrder;

public class IndividualPositionsScorer extends SeriesRaceScorer {

    public IndividualPositionsScorer(final SeriesRace race) {

        super(race);
    }

    @Override
    public Performance getIndividualRacePerformance(final Runner runner, final SingleRaceInternal individual_race) {

        if (individual_race == null) return null;

        // The first finisher of each gender gets the maximum score, the next finisher one less, and so on.

        final List<SingleRaceResult> gender_results = individual_race.getResultsCalculator().getOverallResults().stream().
            map(result -> (SingleRaceResult) result).
            filter(SingleRaceResult::canComplete).
            filter(result -> result.getCategory().getGender().equals(runner.getCategory().getGender())).
            toList();

        final int gender_position = (int) gender_results.stream().
            takeWhile(result -> !result.getParticipant().equals(runner)).
            count() + 1;

        // Higher score is better.
        return gender_position <= gender_results.size() ?
            new Performance(Math.max(score_for_first_place - gender_position + 1, 0)) :
            null;
    }

    @Override
    public Performance getSeriesPerformance(final Runner runner) {

        final SeriesRaceResult series_result = ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getOverallResult(runner);
        final int number_of_counting_scores = Math.min(minimum_number_of_races, numberOfRacesCompleted(series_result));

        // Consider the highest scores, since higher score is better.
        return new Performance(series_result.performances.stream().
            filter(Objects::nonNull).
            map(obj -> (int) obj.getValue()).
            sorted(reverseOrder()).
            limit(number_of_counting_scores).
            reduce(0, Integer::sum));
    }

    @Override
    public int compareSeriesPerformance(final Performance series_result1, final Performance series_result2) {

        final Comparator<Performance> reverse_comparator = ((Comparator<Performance>) super::compareSeriesPerformance).reversed();

        return reverse_comparator.compare(series_result1, series_result2);
    }
}
