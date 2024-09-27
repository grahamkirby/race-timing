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
package org.grahamkirby.race_timing.series_race.fife_ac_midweek;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.series_race.SeriesRaceResult;

import java.util.ArrayList;
import java.util.List;

public class MidweekRaceResult extends SeriesRaceResult {

    public final List<Integer> scores;

    public MidweekRaceResult(final Runner runner, final MidweekRace race) {

        super(runner, race);
        scores = new ArrayList<>();
    }

    @Override
    public int comparePerformanceTo(RaceResult o) {

        // Negate so that a higher score gives an earlier ranking.
        return -Integer.compare(totalScore(), ((MidweekRaceResult) o).totalScore());
    }

    @Override
    public int compareTo(final RaceResult other) {
        return compare(this, other);
    }

    public static int compare(final RaceResult r1, final RaceResult r2) {

        final int compare_completion = r1.compareCompletionTo(r2);
        if (compare_completion != 0) return compare_completion;

        // Either both have completed or neither have completed.

        final int compare_performance = r1.comparePerformanceTo(r2);
        if (compare_performance != 0) return compare_performance;

        // Both have the same points.

        return ((MidweekRaceResult)r1).compareRunnerNameTo((MidweekRaceResult) r2);
    }

    protected int totalScore() {

        int total = 0;

        final List<Integer> sorted_scores = new ArrayList<>(scores);
        sorted_scores.sort(Integer::compareTo);

        for (int i = 0; i < ((MidweekRace)race).getMinimumNumberOfRaces(); i++) {
            final int score = sorted_scores.get(sorted_scores.size() - 1 - i);
            if (score > -1) total += score;
        }

        return total;
    }
}
