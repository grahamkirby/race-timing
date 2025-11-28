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
import org.grahamkirby.race_timing.common.ScorePerformance;
import org.grahamkirby.race_timing.common.SingleRaceInternal;
import org.grahamkirby.race_timing.common.SingleRaceResult;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsCalculator;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.Comparator;
import java.util.List;

public class IndividualPositionsScorer extends SeriesRaceScorer {

    public IndividualPositionsScorer(final SeriesRace race) {

        super(race);
    }

    @Override
    public Performance getIndividualRacePerformance(final Runner runner, final SingleRaceInternal individual_race) {

        if (individual_race == null) return null;

        // The first finisher of each gender gets the maximum score, the next finisher one less, and so on.

        final IndividualRaceResultsCalculator calculator = (IndividualRaceResultsCalculator) individual_race.getResultsCalculator();

        final int gender_position = calculator.getGenderPosition(runner.getName(), runner.getClub(), runner.getCategory().getGender());
        final List<SingleRaceResult> gender_results = calculator.getGenderResults(runner.getCategory().getGender());

        // Gender position is greater than number of gender results if the runner did not complete the race.
        return gender_position <= gender_results.size() ?

            // Higher score is better.
            new ScorePerformance(Math.max(score_for_first_place - gender_position + 1, 0)) :
            null;
    }

    @Override
    public Performance getSeriesPerformance(final Runner runner) {

        // Sort the scores with highest first before selecting, since higher score is better.
        return getSeriesPerformance(runner, Comparator.reverseOrder());
    }

    @Override
    public int compareSeriesPerformance(final Performance series_result1, final Performance series_result2) {

        // Reverse order since higher score is better.
        return -super.compareSeriesPerformance(series_result1, series_result2);
    }
}
