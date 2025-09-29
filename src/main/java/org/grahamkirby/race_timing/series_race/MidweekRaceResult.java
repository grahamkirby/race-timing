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
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.reverseOrder;

class MidweekRaceResult extends SeriesRaceResult {

    private final List<Integer> scores;

    MidweekRaceResult(final Runner runner, final List<Integer> scores, final Race race) {

        super(race, runner);
        this.scores = scores;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        // Sort highest scores first.
        final Comparator<Integer> comparator = reverseOrder();
        final int other_score = ((MidweekRaceResult) other).totalScore();

        return comparator.compare(totalScore(), other_score);
    }

    int totalScore() {

        final int number_of_counting_scores = Math.min(minimum_number_of_races, numberOfRacesCompleted());

        // Consider the highest scores, since higher score is better.
        return scores.stream().
            sorted(reverseOrder()).
            limit(number_of_counting_scores).
            reduce(0, Integer::sum);
    }
}
