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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RacePrizes {

    protected final Race race;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RacePrizes(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void allocatePrizes() {

        for (final PrizeCategory category : race.getPrizeCategories())
            setPrizeWinners(category);
    }

    protected boolean prizeWinner(final RaceResult result, final PrizeCategory category) {

        return !alreadyWonPrize(result) && result.completed() && race.entryCategoryIsEligibleForPrizeCategory(result.getCategory(), category);
    }

    protected boolean alreadyWonPrize(RaceResult result) {

        return result.category_of_prize_awarded != null;
    }

    protected void setPrizeWinner(final RaceResult result, final PrizeCategory category) {

        result.category_of_prize_awarded = category;
    }

    public List<RaceResult> getPrizeWinners(final PrizeCategory category) {

        final List<RaceResult> prize_results = race.getOverallResults().stream().
            filter(result -> result.category_of_prize_awarded != null).
            filter(result -> result.category_of_prize_awarded.equals(category)).
            toList();

        race.setPositionStrings(prize_results, race.allowEqualPositions());

        return prize_results;
    }

    public void setPrizeWinners(final PrizeCategory category) {

        final AtomicInteger position = new AtomicInteger(1);

        race.getOverallResults().stream().
            filter(result -> position.get() <= category.numberOfPrizes() && prizeWinner(result, category)).
            peek(result -> setPrizeWinner(result, category)).
            forEach(_ -> position.getAndIncrement());
    }
}
