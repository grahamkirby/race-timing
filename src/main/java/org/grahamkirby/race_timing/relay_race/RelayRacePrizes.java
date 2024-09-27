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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.Category;

import java.util.ArrayList;
import java.util.List;

public class RelayRacePrizes extends RacePrizes {

    public RelayRacePrizes(final Race race) {

        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void allocatePrizes() {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.
        allocateFirstPrizes();

        // Now consider other prizes (only available in senior categories).
        allocateMinorPrizes();
    }

    private void allocateFirstPrizes() {

        for (final Category category : race.categories.getPrizeCategoriesInDecreasingGeneralityOrder()) {

            for (final RaceResult result : race.getOverallResults()) {

                if (prizeWinner(result, category)) {
                    race.prize_winners.put(category, new ArrayList<>(List.of(result)));
                    break;
                }
            }
        }
    }

    private void allocateMinorPrizes() {

        for (final Category category : race.categories.getPrizeCategoriesInDecreasingGeneralityOrder())
            allocateMinorPrizes(category);
    }

    private void allocateMinorPrizes(final Category category) {

        int position = 2;

        for (final RaceResult result : race.getOverallResults()) {

            if (position > category.numberOfPrizes()) return;

            if (prizeWinner(result, category)) {
                race.prize_winners.get(category).add(result);
                position++;
            }
        }
    }
}
