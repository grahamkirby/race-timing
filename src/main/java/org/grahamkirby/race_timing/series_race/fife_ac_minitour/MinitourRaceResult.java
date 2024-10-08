/*
 * Copyright 2024 Graham Kirby:
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
package org.grahamkirby.race_timing.series_race.fife_ac_minitour;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MinitourRaceResult extends SeriesRaceResult {

    public final List<Duration> times;

    public MinitourRaceResult(final Runner runner, final MinitourRace race) {

        super(runner, race);
        times = new ArrayList<>();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(final RaceResult other) {

        return compare(this, other);
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        return duration().compareTo(((MinitourRaceResult) other).duration());
    }

    public boolean completedAllRacesSoFar() {

        final List<IndividualRace> races = ((MinitourRace)race).getRaces();

        for (int i = 0; i < races.size(); i++)
            if (races.get(i) != null && times.get(i) == null)
                return false;

        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Duration duration() {

        Duration overall = Duration.ZERO;

        for (final Duration time : times)
            if (time != null)
                overall = overall.plus(time);

        return overall;
    }

    public static int compare(final RaceResult r1, final RaceResult r2) {
        
        final int compare_completion = r1.compareCompletionTo(r2);
        if (compare_completion != 0) return compare_completion;

        // Either both have completed or neither have.

        final int compare_completion_so_far = ((MinitourRaceResult) r1).compareCompletionSoFarTo((MinitourRaceResult) r2);
        if (compare_completion_so_far != 0) return compare_completion_so_far;

        if (((MinitourRaceResult) r1).completedAllRacesSoFar()) {

            final int compare_performance = r1.comparePerformanceTo(r2);
            if (compare_performance != 0) return compare_performance;
        }

        return ((MinitourRaceResult) r1).compareRunnerNameTo((MinitourRaceResult) r2);
    }

    private int compareCompletionSoFarTo(final MinitourRaceResult o) {

        if (completedAllRacesSoFar() && !o.completedAllRacesSoFar()) return -1;
        if (!completedAllRacesSoFar() && o.completedAllRacesSoFar()) return 1;

        return 0;
    }
}
