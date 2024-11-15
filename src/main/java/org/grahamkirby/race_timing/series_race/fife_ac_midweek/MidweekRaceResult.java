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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceResult;

import java.util.ArrayList;
import java.util.List;

public class MidweekRaceResult extends SeriesRaceResult {

    protected final List<Integer> scores;

    public MidweekRaceResult(final Runner runner, final Race race) {

        super(runner, race);
        scores = new ArrayList<>();
    }

    @Override
    protected String getIndividualRunnerName() {
        return runner.name;
    }

    @Override
    public int comparePerformanceTo(final RaceResult o) {

        // Negate so that a higher score gives an earlier ranking.
        return -Integer.compare(totalScore(), ((MidweekRaceResult) o).totalScore());
    }

    public boolean hasCompletedAnyRace() {

//        final List<IndividualRace> races = ((MinitourRace)race).getRaces();

        // TODO with case where a runner did complete a large race with over 200 m/f finishers but got zero
        for (int i = 0; i < scores.size(); i++)
//            for (int i = 0; i < races.size(); i++)
            if (scores.get(i) > 0)
//                if (races.get(i) != null && !times.get(i).equals(Race.DUMMY_DURATION))
//                if (races.get(i) != null && times.get(i) == null)
                return true;

        return false;
    }

    protected int totalScore() {

        int total = 0;

        final List<Integer> sorted_scores = new ArrayList<>(scores);
        sorted_scores.sort(Integer::compareTo);

        for (int i = 0; i < ((MidweekRace)race).getMinimumNumberOfRaces(); i++) {
            final int score = sorted_scores.get(sorted_scores.size() - 1 - i);
            if (score > 0) total += score;
        }

        return total;
    }

    public boolean shouldDisplayPosition() {

        final SeriesRace series_race = (SeriesRace) race;
        final int number_of_races_taken_place = series_race.getNumberOfRacesTakenPlace();

        return number_of_races_taken_place < series_race.getRaces().size() || completed();
    }
}
