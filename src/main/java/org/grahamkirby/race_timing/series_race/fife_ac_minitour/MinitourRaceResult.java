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

import org.grahamkirby.race_timing.common.Race;
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
    protected String getIndividualRunnerName() {
        return runner.name;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        return duration().compareTo(((MinitourRaceResult) other).duration());
    }

    @Override
    public boolean shouldDisplayPosition() {
        return completedAllRacesSoFar();
    }

    public boolean completedAllRacesSoFar() {

        final List<IndividualRace> races = ((MinitourRace)race).getRaces();

        for (int i = 0; i < races.size(); i++)
            if (races.get(i) != null && times.get(i).equals(Race.DUMMY_DURATION))
//                if (races.get(i) != null && times.get(i) == null)
                return false;

        return true;
    }

    public boolean completedAnyRacesSoFar() {

        final List<IndividualRace> races = ((MinitourRace)race).getRaces();

        for (int i = 0; i < times.size(); i++)
//            for (int i = 0; i < races.size(); i++)
            if (times.get(i) != null && !times.get(i).equals(Race.DUMMY_DURATION))
//                if (races.get(i) != null && !times.get(i).equals(Race.DUMMY_DURATION))
//                if (races.get(i) != null && times.get(i) == null)
                return true;

        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Duration duration() {

        Duration overall = Duration.ZERO;

        for (int i = 0; i < ((MinitourRace)race).getNumberOfRacesTakenPlace(); i++) {
            Duration time = times.get(i);
//            if (time == null) return IndividualRace.DUMMY_DURATION;
            if (time.equals(Race.DUMMY_DURATION)) return Race.DUMMY_DURATION;
            overall = overall.plus(time);
        }

        return overall;
    }

    int compareCompletionSoFarTo(final MinitourRaceResult o) {

        if (completedAllRacesSoFar() && !o.completedAllRacesSoFar()) return -1;
        if (!completedAllRacesSoFar() && o.completedAllRacesSoFar()) return 1;

        return 0;
    }
}
