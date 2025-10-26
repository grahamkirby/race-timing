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

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.VERY_LONG_DURATION;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

class TourRaceResult extends SeriesRaceResult implements RaceResultWithDuration {

    public final List<Object> times;

    TourRaceResult(final Runner runner, final List<Object> times, final RaceInternal race) {

        super(race, runner);
        this.times = times;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration other_duration = ((TourRaceResult) other).duration();

        return duration().compareTo(other_duration);
    }

    @Override
    public String getPrizeDetail() {

        return "(" + ((Runner) getParticipant()).getClub() + ") " + renderDuration(duration());
    }

    public Duration duration() {

        if (!canComplete()) return VERY_LONG_DURATION;

        return times.stream().
            filter(Objects::nonNull).
            map(obj -> (Duration) obj).
            reduce(Duration.ZERO, Duration::plus);
    }

    public List<Comparator<RaceResult>> getComparators() {

        return List.of(
            CommonRaceResult::comparePossibleCompletion,
            CommonRaceResult::comparePerformance,
            CommonRaceResult::compareRunnerLastName,
            CommonRaceResult::compareRunnerFirstName);
    }
}
