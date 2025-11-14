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

import org.grahamkirby.race_timing.common.RaceResults;
import org.grahamkirby.race_timing.common.RawResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface RelayRaceResults extends RaceResults {

    List<? extends RawResult> getRawResults();

    int getNumberOfLegs();
    List<RelayRaceLegResult> getLegResults(int leg);
    List<String> getLegDetails(RelayRaceResult result);
    List<Boolean> getPairedLegs();
    Map<Integer, Integer> countLegsFinishedPerTeam();
    Map<RawResult, Integer> getExplicitlyRecordedLegNumbers();

    List<Integer> getBibNumbersWithMissingTimes(Map<Integer, Integer> legsFinishedPerTeam);
    List<Duration> getTimesWithMissingBibNumbers();
}
