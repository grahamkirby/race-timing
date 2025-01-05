/*
 * Copyright 2025 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race.grand_prix;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceResult;

import java.util.List;

public class GrandPrixRaceResult extends SeriesRaceResult {

    private final List<Double> scores;

    GrandPrixRaceResult(final Runner runner, final List<Double> scores, final Race race) {

        super(runner, race);
        this.scores = scores;
    }

    static boolean hasCompletedRaceCategory(final GrandPrixRaceResult result, final RaceCategory category) {

        return category.race_numbers().stream().
            anyMatch(race_number -> race_number <= result.scores.size() && result.scores.get(race_number - 1) > 0.0);
    }

    @Override
    protected String getIndividualRunnerName() {
        return runner.name;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        return Double.compare(totalScore(), ((GrandPrixRaceResult) other).totalScore());
    }

    double totalScore() {

        final int minimum_number_of_races = ((SeriesRace) race).getMinimumNumberOfRaces();
        final int number_of_races_completed = numberOfRacesCompleted();
        final int number_of_counting_scores = Math.min(minimum_number_of_races, number_of_races_completed);

        return scores.stream().
            sorted().
            filter(score -> score > 0).
            limit(number_of_counting_scores).
            reduce(0.0, Double::sum);
    }
}
