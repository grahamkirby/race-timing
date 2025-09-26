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


import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static org.grahamkirby.race_timing.common.Config.KEY_MINIMUM_NUMBER_OF_RACES;
import static org.grahamkirby.race_timing.common.Config.KEY_NUMBER_OF_RACES_IN_SERIES;

class MidweekRaceResult extends RaceResult {

    final Runner runner;
    private final List<Integer> scores;

    private final int minimum_number_of_races;
    private final int number_of_races_in_series;
    private final int number_of_races_taken_place;

    MidweekRaceResult(final Runner runner, final List<Integer> scores, final Race race) {

        super(race, runner);
        this.runner = runner;
        this.scores = scores;

        minimum_number_of_races = (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);
        number_of_races_in_series = (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES);
        number_of_races_taken_place = ((MidweekRaceImpl) race.getSpecific()).getNumberOfRacesTakenPlace();
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        // Negate so that a higher score gives an earlier ranking.
        return -Integer.compare(totalScore(), ((MidweekRaceResult) other).totalScore());
    }

    int totalScore() {

        final int number_of_races_completed = numberOfRacesCompleted();
        final int number_of_counting_scores = Math.min(minimum_number_of_races, number_of_races_completed);

        return -scores.stream().
            map(score -> -score).
            sorted().
            limit(number_of_counting_scores).
            reduce(0, Integer::sum);
    }

    @Override
    public boolean canComplete() {

        final int number_of_races_remaining = number_of_races_in_series - number_of_races_taken_place;

        return numberOfRacesCompleted() + number_of_races_remaining >= minimum_number_of_races;
    }

    @Override
    public boolean shouldDisplayPosition() {

        return canComplete();
    }

    public boolean hasCompletedSeries() {

        return numberOfRacesCompleted() >= minimum_number_of_races;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private int numberOfRacesCompleted() {

        final MidweekRaceImpl race_impl = (MidweekRaceImpl) race.getSpecific();

        return (int) race_impl.getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            filter(result -> result.getParticipant().equals(runner)).
            filter(SingleRaceResult::canComplete).
            count();
    }
}
