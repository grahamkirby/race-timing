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

    @Override
    public boolean canComplete() {

        return super.canComplete() &&
            ((GrandPrixRaceImpl) race.getSpecific()).getRaceCategories().stream().allMatch(this::canCompleteRaceCategory);
    }

    @Override
    public boolean hasCompletedSeries() {

        // TODO tests pass without check for race category completion - add test.
        return super.hasCompletedSeries() &&
            ((GrandPrixRaceImpl) race.getSpecific()).getRaceCategories().stream().allMatch(this::hasCompletedRaceCategory);
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        // Sort lowest scores first since lower score is better.
        return Integer.compare(totalScore(), ((GrandPrixRaceResult) other).totalScore());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    int totalScore() {

        final int number_of_counting_scores = Math.min(minimum_number_of_races, numberOfRacesCompleted());

        // Consider the lowest non-zero scores, since lower score is better.
        return scores.stream().
            filter(score -> score > 0).
            sorted().
            limit(number_of_counting_scores).
            reduce(0, Integer::sum);
    }

    boolean hasCompletedRaceCategory(final GrandPrixRaceCategory category) {

        return category.race_numbers().stream().
            anyMatch(this::hasCompletedRace);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean hasCompletedRace(final int race_number) {

        return race_number <= scores.size() && scores.get(race_number - 1) > 0;
    }

    private boolean canCompleteRaceCategory(final GrandPrixRaceCategory category) {

        final List<Race> races = ((SeriesRace) race.getSpecific()).getRaces();

        final int number_of_races_required_in_category = category.minimum_number_to_be_completed();
        final int number_of_races_completed_in_category = numberOfRacesCompletedInCategory(category);
        final int number_of_races_remaining_in_category = numberOfRacesRemainingInCategory(races, category);

        return number_of_races_completed_in_category + number_of_races_remaining_in_category >= number_of_races_required_in_category;
    }

    private int numberOfRacesCompletedInCategory(final GrandPrixRaceCategory category) {

        return (int) category.race_numbers().stream().
            filter(this::hasCompletedRace).
            count();
    }

    private int numberOfRacesRemainingInCategory(final List<Race> races, final GrandPrixRaceCategory category) {

        // TODO tests pass when filter is for non null.
        return (int) category.race_numbers().stream().
            map(race_number -> races.get(race_number - 1)).
            filter(Objects::isNull).
            count();
    }
}
