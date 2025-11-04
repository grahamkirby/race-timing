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

import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.KEY_MINIMUM_NUMBER_OF_RACES;
import static org.grahamkirby.race_timing.common.Config.KEY_NUMBER_OF_RACES_IN_SERIES;

public class SeriesRaceResult extends CommonRaceResult {

    protected final List<Performance> performances;

    protected final int minimum_number_of_races;
    protected final int number_of_races_in_series;
    protected final int number_of_races_taken_place;

    private final SeriesRaceScorer scorer;

    public SeriesRaceResult(final RaceInternal race, final Participant participant, final List<Performance> performances) {

        super(race, participant);

        this.performances = performances;
        scorer = ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getScorer();

        minimum_number_of_races = (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);
        number_of_races_in_series = (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES);
        number_of_races_taken_place = ((SeriesRace) race).getNumberOfRacesTakenPlace();
    }

    @Override
    public String getPrizeDetail() {

        final Runner runner = (Runner) getParticipant();
        return "(" + runner.getClub() + ") " + scorer.getSeriesPerformance(runner);
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Runner runner = (Runner) getParticipant();

        return scorer.compareSeriesPerformance(scorer.getSeriesPerformance(runner),
            scorer.getSeriesPerformance((Runner) other.getParticipant()));
    }

    @Override
    public boolean canComplete() {

        final int number_of_races_remaining = number_of_races_in_series - number_of_races_taken_place;

        final boolean has_completed_all_race_categories =
            ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getRaceCategories().stream().allMatch(this::canCompleteRaceCategory);

        return has_completed_all_race_categories && numberOfRacesCompleted() + number_of_races_remaining >= minimum_number_of_races;
    }

    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return List.of(
            CommonRaceResult::comparePossibleCompletion,
            SeriesRaceResult::compareNumberOfRacesCompleted,
            CommonRaceResult::comparePerformance,
            CommonRaceResult::compareRunnerLastName,
            CommonRaceResult::compareRunnerFirstName);
    }

    public boolean hasCompletedSeries() {

        return numberOfRacesCompleted() >= minimum_number_of_races &&
            ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getRaceCategories().stream().allMatch(this::hasCompletedRaceCategory);

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    boolean hasCompletedRaceCategory(final SeriesRaceCategory category) {

        return category.race_numbers().stream().
            anyMatch(this::hasCompletedRace);
    }

    int numberOfRacesCompleted() {

        return (int) ((SeriesRace) race).getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            filter(result -> result.getParticipant().equals(participant)).
            filter(SingleRaceResult::canComplete).
            count();
    }

    static int compareNumberOfRacesCompleted(final RaceResult r1, final RaceResult r2) {

        final int minimum_number_of_races = (int) r1.getRace().getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);

        final int relevant_number_of_races_r1 = Math.min(((SeriesRaceResult) r1).numberOfRacesCompleted(), minimum_number_of_races);
        final int relevant_number_of_races_r2 = Math.min(((SeriesRaceResult) r2).numberOfRacesCompleted(), minimum_number_of_races);

        return -Integer.compare(relevant_number_of_races_r1, relevant_number_of_races_r2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

    private boolean hasCompletedRace(final int race_number) {

        return race_number <= performances.size() && performances.get(race_number - 1) != null;
    }

    private int numberOfRacesRemainingInCategory(final List<SingleRaceInternal> races, final SeriesRaceCategory category) {

        // TODO tests pass when filter is for non null.
        return (int) category.race_numbers().stream().
            map(race_number -> races.get(race_number - 1)).
            filter(Objects::isNull).
            count();
    }
}
