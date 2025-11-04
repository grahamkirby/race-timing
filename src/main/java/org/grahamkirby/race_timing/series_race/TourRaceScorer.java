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

import org.grahamkirby.race_timing.common.SingleRaceInternal;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.time.Duration;
import java.util.Objects;

public class TourRaceScorer extends SeriesRaceScorer {

    public TourRaceScorer(final SeriesRace race) {

        super(race);
    }

    @Override
    public Performance getIndividualRacePerformance(final Runner runner, final SingleRaceInternal individual_race) {

        if (individual_race == null) return null;

        return new Performance(getIndividualRaceTime(runner, individual_race));
    }

    @Override
    public Performance getSeriesPerformance(final Runner runner) {

        final SeriesRaceResult series_result = ((SeriesRaceResultsCalculator) race.getResultsCalculator()).getOverallResult(runner);

        if (!series_result.canComplete()) return null;

        return new Performance(series_result.performances.stream().
            filter(Objects::nonNull).
            map(obj -> (Duration) obj.getValue()).
            reduce(Duration.ZERO, Duration::plus));
    }
}
