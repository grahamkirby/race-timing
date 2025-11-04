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
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.SingleRaceInternal;
import org.grahamkirby.race_timing.common.SingleRaceResult;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.Comparator;
import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.*;

public abstract class SeriesRaceScorer {

    protected final SeriesRace race;

    protected final int minimum_number_of_races;
    protected int score_for_first_place;
    protected int score_for_median_position;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected SeriesRaceScorer(final SeriesRace race) {

        this.race = race;

        minimum_number_of_races = (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);

        if (race.getConfig().containsKey(KEY_SCORE_FOR_FIRST_PLACE))
            score_for_first_place = (int) race.getConfig().get(KEY_SCORE_FOR_FIRST_PLACE);
        if (race.getConfig().containsKey(KEY_SCORE_FOR_MEDIAN_POSITION))
            score_for_median_position = (int) race.getConfig().get(KEY_SCORE_FOR_MEDIAN_POSITION);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public int compareSeriesPerformance(final Performance series_performance1, final Performance series_performance2) {

        final Comparator<Performance> comparator = Comparator.nullsLast(Performance::compareTo);

        return comparator.compare(series_performance1, series_performance2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected int numberOfRacesCompleted(final SeriesRaceResult series_result) {

        return (int) series_result.performances.stream().filter(Objects::nonNull).count();
    }

    protected Performance getIndividualRaceTime(final Runner runner, final SingleRaceInternal individual_race) {

        if (individual_race == null) return null;

        for (final RaceResult result : individual_race.getResultsCalculator().getOverallResults()) {

            final SingleRaceResult individual_result = (SingleRaceResult) result;
            if (individual_result.getParticipant().equals(runner))
                return individual_result.getPerformance();
        }

        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    abstract Performance getIndividualRacePerformance(Runner runner, SingleRaceInternal individual_race);
    abstract Performance getSeriesPerformance(Runner runner);
}
