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

import java.util.List;
import java.util.Objects;

class GrandPrixRaceResult extends SeriesRaceResult {

    private final List<Integer> scores;

    GrandPrixRaceResult(final Runner runner, final List<Integer> scores, final Race race) {

        super(race, runner);
        this.scores = scores;
    }

    boolean hasCompletedRaceCategory(final GrandPrixRaceCategory category) {

        return category.race_numbers().stream().
            anyMatch(this::hasCompletedRace);
    }

    public boolean hasCompletedSeries() {

        // TODO tests pass without check for race category completion - add test.
        return numberOfRacesCompleted() >= minimum_number_of_races &&
            ((GrandPrixRaceImpl) race.getSpecific()).getRaceCategories().stream().allMatch(this::hasCompletedRaceCategory);
    }

    private boolean hasCompletedRace(final int race_number) {
        return race_number <= scores.size() && scores.get(race_number - 1) > 0.0;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        return Integer.compare(totalScore(), ((GrandPrixRaceResult) other).totalScore());
    }

    @Override
    public boolean canComplete() {

        final int number_of_races_remaining = number_of_races_in_series - number_of_races_taken_place;

        return numberOfRacesCompleted() + number_of_races_remaining >= minimum_number_of_races &&
            ((GrandPrixRaceImpl) race.getSpecific()).getRaceCategories().stream().allMatch(this::canCompleteRaceCategory);
    }

    @Override
    public boolean shouldDisplayPosition() {
        return canComplete();
    }

    private boolean canCompleteRaceCategory(final GrandPrixRaceCategory category) {

        final int number_of_races_remaining_in_category = (int) category.race_numbers().stream().
            map(race_number -> ((GrandPrixRaceImpl) race.getSpecific()).getRaces().get(race_number - 1)).
            filter(Objects::nonNull).
            count();

        final int number_of_races_required_in_category = category.minimum_number_to_be_completed();

        final int number_of_races_completed_in_category = (int) category.race_numbers().stream().
            filter(this::hasCompletedRace).
            count();

        return number_of_races_completed_in_category + number_of_races_remaining_in_category >= number_of_races_required_in_category;
    }

    int totalScore() {

        final int number_of_races_completed = numberOfRacesCompleted();
        final int number_of_counting_scores = Math.min(minimum_number_of_races, number_of_races_completed);

        return scores.stream().
            sorted().
            filter(score -> score > 0).
            limit(number_of_counting_scores).
            reduce(0, Integer::sum);
    }
}
