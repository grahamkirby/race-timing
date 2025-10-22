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
package org.grahamkirby.race_timing.relay_race;


import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Config.VERY_LONG_DURATION;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

public class RelayRaceResult extends SingleRaceResult {

    private final List<RelayRaceLegResult> leg_results;

    RelayRaceResult(final Race2 race, final RaceEntry entry, final Duration finish_time) {

        super(race, entry, finish_time);

        leg_results = new ArrayList<>();

        final int number_of_legs = ((RelayRace) race).getNumberOfLegs();

        for (int i = 0; i < number_of_legs; i++)
            leg_results.add(new RelayRaceLegResult(race, entry));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canComplete() {

        return leg_results.stream().allMatch(RelayRaceLegResult::canComplete);
    }

    @Override
    public Duration duration() {

        return !canComplete() ? VERY_LONG_DURATION :
            leg_results.stream().
                map(RelayRaceLegResult::duration).
                reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return List.of(
            CommonRaceResult::comparePossibleCompletion,
            CommonRaceResult::comparePerformance,
            RelayRaceResult::compareTeamName);
    }

    @Override
    public String getPrizeDetail() {

        return "(" + getParticipant().getCategory().getLongName() + ") " + renderDuration(this, DNF_STRING);
    }

    public List<RelayRaceLegResult> getLegResults() {
        return leg_results;
    }

    public RelayRaceLegResult getLegResult(final int leg_number) {
        return leg_results.get(leg_number - 1);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Compares two results based on alphabetical ordering of the team name. */
    static int compareTeamName(final RaceResult r1, final RaceResult r2) {

        return r1.getParticipantName().compareToIgnoreCase(r2.getParticipantName());
    }
}
