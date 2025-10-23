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

import org.grahamkirby.race_timing.common.*;

import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.KEY_MINIMUM_NUMBER_OF_RACES;
import static org.grahamkirby.race_timing.common.Config.KEY_NUMBER_OF_RACES_IN_SERIES;

public abstract class SeriesRaceResult extends CommonRaceResult {

    protected final int minimum_number_of_races;
    protected final int number_of_races_in_series;
    protected final int number_of_races_taken_place;

    public SeriesRaceResult(final SingleRaceInternal race, final Participant participant) {

        super(race, participant);

        minimum_number_of_races = (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);
        number_of_races_in_series = (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES);
        number_of_races_taken_place = ((SeriesRace) race).getNumberOfRacesTakenPlace();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canComplete() {

        final int number_of_races_remaining = number_of_races_in_series - number_of_races_taken_place;

        return numberOfRacesCompleted() + number_of_races_remaining >= minimum_number_of_races;
    }

    @Override
    public String getPrizeDetail() {

        return null;
    }

    public boolean hasCompletedSeries() {

        return numberOfRacesCompleted() >= minimum_number_of_races;
    }

    protected int numberOfRacesCompleted() {

        return (int) ((SeriesRace) race).getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            filter(result -> result.getParticipant().equals(participant)).
            filter(SingleRaceResult::canComplete).
            count();
    }

    public static int compareNumberOfRacesCompleted(final RaceResult r1, final RaceResult r2) {

        final int minimum_races_to_qualify = (int) r1.getRace().getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);

        final int relevant_number_of_races_r1 = Math.min(((SeriesRaceResult) r1).numberOfRacesCompleted(), minimum_races_to_qualify);
        final int relevant_number_of_races_r2 = Math.min(((SeriesRaceResult) r2).numberOfRacesCompleted(), minimum_races_to_qualify);

        return -Integer.compare(relevant_number_of_races_r1, relevant_number_of_races_r2);
    }
}
