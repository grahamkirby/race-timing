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

import java.util.List;
import java.util.Objects;

class GrandPrixRaceResult extends SeriesRaceResult {

    GrandPrixRaceResult(final Runner runner, final List<Object> performances, final RaceInternal race) {

        super(race, runner,  performances);
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        // Sort lowest scores first since lower score is better.
        final int other_score = ((GrandPrixRaceResult) other).totalScore();

        return Integer.compare(totalScore(), other_score);
    }

    @Override
    public String getPrizeDetail() {

        return "(" + ((Runner) getParticipant()).getClub() + ") " + totalScore();
    }

    @Override
    public boolean canComplete() {

        return super.canComplete() &&
            ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getRaceCategories().stream().allMatch(this::canCompleteRaceCategory);
    }

    @Override
    public boolean hasCompletedSeries() {

        // TODO tests pass without check for race category completion - add test.
        return super.hasCompletedSeries() &&
            ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getRaceCategories().stream().allMatch(this::hasCompletedRaceCategory);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    int totalScore() {

        final int number_of_counting_scores = Math.min(minimum_number_of_races, numberOfRacesCompleted());

        // Consider the lowest scores, since lower score is better.
        return performances.stream().
            filter(Objects::nonNull).
            map(obj -> (int) obj).
            sorted().
            limit(number_of_counting_scores).
            reduce(0, Integer::sum);
    }

    boolean hasCompletedRaceCategory(final SeriesRaceCategory category) {

        return category.race_numbers().stream().
            anyMatch(this::hasCompletedRace);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean hasCompletedRace(final int race_number) {

        return race_number <= performances.size() && performances.get(race_number - 1) != null;
    }

    private boolean canCompleteRaceCategory(final SeriesRaceCategory category) {

        final List<SingleRaceInternal> races = ((SeriesRace) race).getRaces();

        final int number_of_races_required_in_category = category.minimum_number_to_be_completed();
        final int number_of_races_completed_in_category = numberOfRacesCompletedInCategory(category);
        final int number_of_races_remaining_in_category = numberOfRacesRemainingInCategory(races, category);

        return number_of_races_completed_in_category + number_of_races_remaining_in_category >= number_of_races_required_in_category;
    }

    private int numberOfRacesCompletedInCategory(final SeriesRaceCategory category) {

        return (int) category.race_numbers().stream().
            filter(this::hasCompletedRace).
            count();
    }

    private int numberOfRacesRemainingInCategory(final List<SingleRaceInternal> races, final SeriesRaceCategory category) {

        // TODO tests pass when filter is for non null.
        return (int) category.race_numbers().stream().
            map(race_number -> races.get(race_number - 1)).
            filter(Objects::isNull).
            count();
    }
}
