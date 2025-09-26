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

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.SingleRaceResult;

import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.KEY_MINIMUM_NUMBER_OF_RACES;
import static org.grahamkirby.race_timing.common.Config.KEY_NUMBER_OF_RACES_IN_SERIES;

public abstract class SeriesRaceResult extends RaceResult {

    protected final int minimum_number_of_races;
    protected final int number_of_races_in_series;
    protected final int number_of_races_taken_place;

    public SeriesRaceResult(final Race race, final Participant participant) {

        super(race, participant);

        minimum_number_of_races = (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);
        number_of_races_in_series = (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES);
        number_of_races_taken_place = ((SeriesRace) race.getSpecific()).getNumberOfRacesTakenPlace();
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////

    protected int numberOfRacesCompleted() {

        return (int) ((SeriesRace) race.getSpecific()).getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            filter(result -> result.getParticipant().equals(participant)).
            filter(SingleRaceResult::canComplete).
            count();
    }
}
