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
package org.grahamkirby.race_timing.series_race.midweek;

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceResult;

import java.util.List;

public class MidweekRaceResult extends SeriesRaceResult {

    private final List<Integer> scores;

    MidweekRaceResult(final Runner runner, final List<Integer> scores, final SeriesRace race) {

        super(runner, race);
        this.scores = scores;
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

        // Negate so that a higher score gives an earlier ranking.
        return -Integer.compare(totalScore(), ((MidweekRaceResult) other).totalScore());
    }

    int totalScore() {

        final int number_of_races_to_count = Math.min(
            ((SeriesRace) race).getNumberOfRacesTakenPlace(),
            ((SeriesRace) race).getMinimumNumberOfRaces());

        return scores.stream().
            sorted().
            toList().
            reversed().
            stream().
            limit(number_of_races_to_count).
            reduce(0, Integer::sum);
    }
}
