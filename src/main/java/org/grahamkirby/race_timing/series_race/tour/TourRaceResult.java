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
package org.grahamkirby.race_timing.series_race.tour;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.series_race.SeriesRaceResult;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TourRaceResult extends SeriesRaceResult {

    public final List<Duration> times;

    TourRaceResult(final Runner runner, final List<Duration> times, final TourRace race) {

        super(runner, race);
        this.times = times;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((TourRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {

        return !canComplete() ? null :
            times.stream().
                filter(Objects::nonNull).
                reduce(Duration.ZERO, Duration::plus);
    }
}
