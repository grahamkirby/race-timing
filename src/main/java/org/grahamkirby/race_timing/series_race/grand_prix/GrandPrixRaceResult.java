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

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceResult;

import java.util.List;
import java.util.Objects;

class GrandPrixRaceResult extends SeriesRaceResult {

    private final List<Integer> scores;

    GrandPrixRaceResult(final Runner runner, final List<Integer> scores, final SeriesRace race) {

        super(runner, race);
        this.scores = scores;
    }

    boolean hasCompletedRaceCategory(final RaceCategory category) {

        return category.race_numbers().stream().
            anyMatch(this::hasCompletedRace);
    }

    private boolean hasCompletedRace(final int race_number) {
        return race_number <= scores.size() && scores.get(race_number - 1) > 0.0;
    }

    @Override
    protected String getIndividualRunnerName() {
        return runner.name;
    }

    @Override
    public Participant getParticipant() {
        return runner;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        return Double.compare(totalScore(), ((GrandPrixRaceResult) other).totalScore());
    }

    @Override
    public boolean canComplete() {

        return super.canComplete() &&
            ((GrandPrixRace)race).race_categories.stream().allMatch(this::canCompleteRaceCategory);
    }

    private boolean canCompleteRaceCategory(final RaceCategory category) {

        final int number_of_races_remaining_in_category = (int) category.race_numbers().stream().
            map(race_number -> ((SeriesRace) race).getRaces().get(race_number - 1)).
            filter(Objects::nonNull).
            count();

        final int number_of_races_required_in_category = category.minimum_number_to_be_completed();

        final int number_of_races_completed_in_category = (int) category.race_numbers().stream().
            filter(this::hasCompletedRace).
            count();

        return number_of_races_completed_in_category + number_of_races_remaining_in_category >= number_of_races_required_in_category;
    }

    int totalScore() {

        final int minimum_number_of_races = ((SeriesRace) race).getMinimumNumberOfRaces();
        final int number_of_races_completed = numberOfRacesCompleted();
        final int number_of_counting_scores = Math.min(minimum_number_of_races, number_of_races_completed);

        return scores.stream().
            sorted().
            filter(score -> score > 0).
            limit(number_of_counting_scores).
            reduce(0, Integer::sum);
    }
}
