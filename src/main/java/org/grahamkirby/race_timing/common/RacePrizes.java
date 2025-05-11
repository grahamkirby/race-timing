/*
 * Copyright 2025 Graham Kirby:
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

    protected boolean isPrizeWinner(final RaceResult result, final PrizeCategory prize_category) {

        return isStillEligibleForPrize(result, prize_category) &&
            (result.getCompletionStatus() == CompletionStatus.COMPLETED || result.getCompletionStatus() == CompletionStatus.CAN_COMPLETE) &&
            race.isResultEligibleForPrizeCategory(result, prize_category);
    }

    private static boolean isStillEligibleForPrize(final RaceResult result, final PrizeCategory new_prize_category) {

        if (!new_prize_category.isExclusive()) return true;

        for (final PrizeCategory category_already_won : result.categories_of_prizes_awarded)
            if (category_already_won.isExclusive()) return false;

        return true;
    }

    protected static void setPrizeWinner(final RaceResult result, final PrizeCategory category) {

        result.categories_of_prizes_awarded.add(category);
    }

    /** Returns prize winners in given category. */
    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = race.getOverallResults().stream().
            filter(result -> result.categories_of_prizes_awarded.contains(prize_category)).
            toList();

        race.setPositionStrings(prize_results);

        return prize_results;
    }

    /** Sets the prize winners in the given category. */
    private void setPrizeWinners(final PrizeCategory category) {

        final AtomicInteger position = new AtomicInteger(1);

        race.getOverallResults().stream().
            filter(_ -> position.get() <= category.numberOfPrizes()).
            filter(result -> isPrizeWinner(result, category)).
            forEachOrdered(result -> {
                position.getAndIncrement();
                setPrizeWinner(result, category);
            });
    }

    public boolean arePrizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory category2 : race.getPrizeCategories().reversed()) {

            if (!race.prizes.getPrizeWinners(category2).isEmpty()) return true;
            if (category.equals(category2) && !arePrizesInOtherCategorySameAge(category)) return false;
        }
        return false;
    }

    private boolean arePrizesInOtherCategorySameAge(final PrizeCategory category) {

        return race.getPrizeCategories().stream().
            filter(cat -> !cat.equals(category)).
            filter(cat -> cat.getMinimumAge() == category.getMinimumAge()).
            anyMatch(cat -> !race.prizes.getPrizeWinners(cat).isEmpty());
    }
}
