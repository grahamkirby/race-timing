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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.util.ArrayList;
import java.util.List;

public class RacePrizes {

    protected final Race race;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RacePrizes(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void allocatePrizes() {

        for (final PrizeCategory category : race.getPrizeCategories())
            race.prize_winners.put(category, getPrizeWinners(category));
    }

    protected boolean prizeWinner(final RaceResult result, final PrizeCategory category) {

        return result.completed() && race.isEligibleFor(result.getCategory(), category) && notYetWonPrize(result);
    }

    private boolean notYetWonPrize(final RaceResult potential_winner) {

        for (final List<RaceResult> winners : race.prize_winners.values())
            for (final RaceResult result : winners)
                if (result.sameEntrant(potential_winner))
                    return false;

        return true;
    }

    private List<RaceResult> getPrizeWinners(final PrizeCategory category) {

        final List<RaceResult> prize_winners = new ArrayList<>();

        int position = 1;

        for (final RaceResult result : race.getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result);
                position++;
            }
        }
        return prize_winners;
    }
}
