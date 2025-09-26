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

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.KEY_MINIMUM_NUMBER_OF_RACES;
import static org.grahamkirby.race_timing.common.Config.KEY_NUMBER_OF_RACES_IN_SERIES;

class TourRaceResult extends RaceResult {

    public final List<Duration> times;

    TourRaceResult(final Runner runner, final List<Duration> times, final Race race) {

        super(race, runner);
        this.times = times;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((TourRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    public Duration duration() {

        return !canComplete() ? null :
            times.stream().
                filter(Objects::nonNull).
                reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public boolean canComplete() {

        final int number_of_races_remaining = (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) - ((TourRaceImpl) race.getSpecific()).getNumberOfRacesTakenPlace();

        return numberOfRacesCompleted() + number_of_races_remaining >= (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);
    }

    @Override
    public boolean shouldDisplayPosition() {

        return canComplete();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected int numberOfRacesCompleted() {

        return (int) ((TourRaceImpl) race.getSpecific()).getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            filter(result -> result.getParticipant().equals(participant)).
            filter(SingleRaceResult::canComplete).
            count();
    }
}
