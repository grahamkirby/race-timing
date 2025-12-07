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
import org.grahamkirby.race_timing.common.Performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

public class RelayRaceResult extends SingleRaceResult {

    private final List<RelayRaceLegResult> leg_results;

    RelayRaceResult(final RaceInternal race, final RaceEntry entry, final Duration finish_time) {

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
    public Performance getPerformance() {

        return canComplete() ?
            new DurationPerformance(leg_results.stream().
                map(leg_result -> (Duration) leg_result.getPerformance().getValue()).
                reduce(Duration.ZERO, Duration::plus)) :
            null;
    }

    @Override
    public Comparator<RaceResult> getComparator() {

        return consecutiveComparator(
            CommonRaceResult::comparePossibleCompletion,
            CommonRaceResult::comparePerformance,

            conditionalComparator(
                RelayRaceResult::canDistinguishEqualPerformances,
                RelayRaceResult::compareRecordedPosition,
                RelayRaceResult::compareTeamName
            )
        );
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
    private static int compareTeamName(final RaceResult r1, final RaceResult r2) {

        return r1.getParticipantName().compareToIgnoreCase(r2.getParticipantName());
    }

    private static boolean canDistinguishEqualPerformances(final RaceResult result1, final RaceResult result2) {

        final boolean dead_heat1 = result1.getPositionString() != null && result1.getPositionString().endsWith("=");
        final boolean dead_heat2 = result2.getPositionString() != null && result2.getPositionString().endsWith("=");

        final boolean dnf1 = !result1.canComplete();
        final boolean dnf2 = !result2.canComplete();

        return !(dead_heat1 || dead_heat2 || dnf1 || dnf2);
    }

    protected int getRecordedPosition(final int bib_number, final SingleRaceInternal race) {

        return race.getRawResults().size() - (int) race.getRawResults().reversed().stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count() + 1;
    }
}
